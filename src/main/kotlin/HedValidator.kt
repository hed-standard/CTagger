import model.TagModel

class HedValidator(private val schema: HashMap<String, TagModel>, private val tagger: CTagger) {
    fun validateEntry(entry: String): Boolean{
        val entry_trimmed = entry.trim()
        if (tagger.isVerbose) println(entry_trimmed)
        val splitted = entry_trimmed.split('/')
        if (splitted.size < 2) {
            // unique node. Check for valid and give suggestions
            return isPartialEntryValid(entry_trimmed)
        }
        else { // has multiple nodes
            val parentNode = splitted[splitted.size-2]
            val valueInput = splitted.last()
            if (!schema.containsKey(valueInput)) {
                if (!schema.containsKey(parentNode)) {
                    // parent not found in schema --> invalid
                    return false
                }
                else  {
                    // Could be one of these scenarios:
                    // 1. Value of takesValue node --> prev node is requireChild
                    // 2. Extension of extensionAllowed node
                    // 3. Unfinished node typing (e.g. Event/Se) --> previous node is not end node
                    // check based on the previous node
                    val tagModel = schema[parentNode]!!
                    if (tagModel.hasAttribute("childRequired")) {
                        if (tagModel.hasAttribute("takesValue") || schema.containsKey("$parentNode/#")) { // sometimes takesValue attribute is set in the # node
                            tagger.clearSearchResult()
                            val valueNode = schema["$parentNode/#"]!! // all takesValue nodes are followed by a #
                            return validateValueInput(valueInput, valueNode)
                        }
                        else {
                            // not takesValue. Choose valid children or extensionAllowed instead
                            if (tagModel.hasAttribute("extensionAllowed"))
                                // extensionAllowed. Just check for valid tag pattern
                                return validateValueInput(valueInput, tagModel)
                            else {
                                // requiredChild yet not extensionAllowed --> Value must exists in schema. Validating the whole entry
                                return isPartialEntryValid(entry_trimmed)
                            }
                        }
                    }
                    else if (tagModel.hasAttribute("extensionAllowed")) {
                        // check if extended portion is valid
                        return validateValueInput(valueInput, tagModel)
                    }
                    else {
                        // unfinished node typing --> show suggestion
                        return isPartialEntryValid(entry_trimmed)
                    }
                }
            }
            else {
                // last node exists in schema. Check if full path is valid
                // if any of previous nodes was invalid, whole entry will be invalid
                return schema.containsKey(entry_trimmed)
            }
        }
    }
    private fun validateValueInput(typedText: String, tagModel: TagModel): Boolean {
        val validTagPattern = "[\\w|\\+|\\^|-|\\s|\\d|/]*"
        if (!tagModel.hasAttribute("isNumeric") && tagModel.getUnitClass() != "clockTime")
            return Regex(validTagPattern).matches(typedText)
        else {
            if (tagModel.hasAttribute("isNumeric")) {
                val re = Regex("^((-?[0-9]+(\\.[0-9]+)?)|(-\\.[0-9]+))(\\w|\\s)*")
                val matchResult = re.matchEntire(typedText)
                if (matchResult == null)
                    return false
                else {
                    if (matchResult.groups.size == 2) {
                        // has unit
                        val enteredUnit = matchResult.groups[1]!!.value.trim()
                        // validate with allowed units
                        val unitClass = tagger.unitClasses.find {
                            it.name == tagModel.getUnitClass()
                        }
                        if (unitClass != null) {
                            val allowedUnits = mutableListOf<String>()
                            unitClass!!.units.map {
                                if (it.isSIUnit) {
                                    if (it.isUnitSymbol)
                                        tagger.unitModifiers.filter {modifier -> modifier.isSIUnitSymbolModifier}.map {mod -> allowedUnits.add("${mod.name}${it.name}")}
                                    else {
                                        tagger.unitModifiers.filter {modifier -> !modifier.isSIUnitSymbolModifier}.map {mod -> allowedUnits.add("${mod.name}${it.name}")}
                                    }
                                }
                                else
                                    allowedUnits.add(it.name)
                            }
                            return allowedUnits.contains(enteredUnit)
                        }
                    }
                }
            } else if (tagModel.getUnitClass() == "clockTime") {
                return typedText.matches(Regex("^((2[0-3]|[01]?[0-9]):([0-5]?[0-9]))|((2[0-3]|[01]?[0-9]):([0-5]?[0-9]):([0-5]?[0-9]))$"))
            }
        }
        return true
    }

    private fun findMatchingTags(entry: String): List<String> {
        val tags = tagger.tags
        return tags.filter {
//            // parse takeValues node if applicable
//            val splitted = target.split('/')
            it.contains(entry, true)// || (splitted.size >= 2 && it.contains(splitted[splitted.size-2] + "/#", true))
        } // beautiful syntax comparing to Java!
    }

    private fun isPartialEntryValid(entry: String): Boolean {
        val matchedTags = findMatchingTags(entry)
        // if valid partial entry, add matching tags to search result box
        if (matchedTags.isNotEmpty()) {
            tagger.addTagsToSearchResult(matchedTags)
            return true
        }
        return false
    }

}