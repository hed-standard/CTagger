package model

class TagModel(val fullPath: String, var parent: TagModel? = null, xmlModel: TagXmlModel?): Comparable<TagModel> {
    var name: String? = null
    var depth = 0
    var description: String = ""
    var attributes = HashMap<String, List<String>?>()
//    var childRequired = false
//    var extensionAllowed = false
//        set(value) {if (value) attributes.add("extensionAllowed") else attributes.remove("extensionAllowed")}
//    var takesValue = false
//        set(value) {if (value) attributes.add("takesValue") else attributes.remove("extensionAllowed")}
//    var isNumeric = false
//    var required = false
//    var recommended = false
//    var suggestedTag = mutableListOf<String>()
//    var relatedTag = mutableListOf<String>()
//    var unitClass: String? = null
    var children = mutableListOf<TagModel>()

    init {
        if (xmlModel != null) {
            name = xmlModel.name
            description = xmlModel.description
            for (attr in xmlModel.attribute) {
                attributes[attr.name] = attr.values.toMutableList()
            }
        }
    }

    /**
     * Tags are compared by their paths.
     */
    override fun compareTo(tagModel: TagModel): Int {
        return if (fullPath == tagModel.fullPath) 0 else -1
    }

    override fun toString(): String {
        return fullPath
    }

    fun hasAttribute(attr:String): Boolean {
        return attributes.containsKey(attr)
    }

    fun getAttribute(attr:String): List<String> {
        when (hasAttribute(attr)) {
            true -> return attributes[attr]!!
            false -> return listOf()
        }
    }

    fun getUnitClass(): String {
        if (hasAttribute("unitClass"))
            return attributes["unitClass"]!![0]
        return ""
    }

    fun setAttribute(attr: String, value: List<String> = listOf()) {
        attributes[attr] = value
    }
}
