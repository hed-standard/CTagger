import com.google.gson.internal.LinkedTreeMap
import java.awt.event.ItemEvent
import javax.swing.JComboBox

class FieldList(val tagger: CTagger): JComboBox<String>() {
    val fieldMap = HashMap<String, HashMap<String,String>>()
    var fieldAndUniqueCodeMap = HashMap<String, List<String>>()
    val isValueField = HashMap<String, Boolean>()
    val oldFieldAndUniqueCodeMap = HashMap<String, List<String>>()
    fun initializeListener() {
        addItemListener {
            if (it.stateChange == ItemEvent.DESELECTED) {
                val curField = it.item as String
                println(curField)
//                // save current work
//                if (curField != null) {
//                    val map = fieldMap[curField!!]
//                    val key = tagger.eventCodeList.selectedValue
////                    eventCodeList.prevSelected = null
//                    if (map != null && key != null)
//                        map[key] = tagger.hedTagInput.getCleanHEDString()
//                    tagger.hedTagInput.text = null
//                }
                // set new field and new code list
                if (selectedItem != null && fieldAndUniqueCodeMap.containsKey(selectedItem.toString()!!)) {
                    // get unique event codes
                    tagger.eventCodeList.codeSet = fieldAndUniqueCodeMap[selectedItem!!]!!
                }
            }
        }
    }
    fun addField(field:String) {
        // add unique codes to each field, ignoring BIDS default numerical fields
        if (!listOf("duration", "onset", "sample", "stim_file", "HED", "response_time").contains(field)) {
            fieldAndUniqueCodeMap[field] = mutableListOf()
            isValueField[field] = false
        } else {
            fieldAndUniqueCodeMap[field] = listOf("HED")
            isValueField[field] = true
        }
        // initialize fieldMap
        fieldMap[field] = HashMap()
        addItem(field)
    }

    fun addFieldFromColumn(column:Array<String>, isCategorical: Boolean) {
        // assuming first row contains field name
        val field = column[0]
        val uniqueValues = column.slice(1 until column.size).distinct()

        // add unique codes to each field, ignoring BIDS default numerical fields
        if (isCategorical) {
            fieldAndUniqueCodeMap[field] = uniqueValues.toList()
            isValueField[field] = false
        } else {
            fieldAndUniqueCodeMap[field] = listOf("HED")
            isValueField[field] = true
        }
        // initialize fieldMap
        fieldMap[field] = HashMap()
        addItem(field)

    }
    fun addFieldFromDict(field:String, fieldDict: CTagger.BIDSFieldDict) {
        // add field name to dropdown list
        addItem(field)
        // add field to data structure
        // whether a field is categorical is determined by whether Levels has values or not
        if (fieldDict.Levels.isNotEmpty()) {
            fieldAndUniqueCodeMap[field] = fieldDict.Levels.keys.toList()
            isValueField[field] = false
        }
        else if (fieldDict.HED is LinkedTreeMap<*,*>) {
            fieldAndUniqueCodeMap[field] = (fieldDict.HED as LinkedTreeMap<String, String>).keys.toList()
            isValueField[field] = false
        }
        else {
            fieldAndUniqueCodeMap[field] = listOf("HED")
            isValueField[field] = true
        }
        // initialize fieldMap
        fieldMap[field] = HashMap()
        // initialize codeMap, add HED string to code if contained in fieldDict
        val hasHED = (fieldDict.HED is String && fieldDict.HED.toString().isNotEmpty()) || (fieldDict.HED is LinkedTreeMap<*,*>)
        fieldAndUniqueCodeMap[field]!!.forEach {
            fieldMap[field]!![it] = ""
            if (hasHED) {
                if (isValueField[field]!! && it.equals("HED"))
                    fieldMap[field]!![it] = fieldDict.HED.toString()
                else if (fieldDict.HED is LinkedTreeMap<*, *> && (fieldDict.HED as LinkedTreeMap<String, String>).containsKey(it))
                    fieldMap[field]!![it] = (fieldDict.HED as LinkedTreeMap<String,String>)[it].toString()
            }

        }

    }

    fun getHedString(field: String, code:String):String {
        if (fieldMap.containsKey(field)) {
            if (fieldMap[field]!!.containsKey(code))
                return fieldMap[field]!![code]!!
        }
        return ""
    }

    fun isEmpty(): Boolean {
        return fieldMap.isEmpty()
    }
    fun clear() {
        fieldMap.clear()
        fieldAndUniqueCodeMap.clear()
        isValueField.clear()
        oldFieldAndUniqueCodeMap.clear()
        removeAllItems()
    }
}