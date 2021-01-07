class TagModel(val path: String, xmlModel: TagXmlModel): Comparable<TagModel> {
    var name: String? = null
    var parentPath: String? = null
    var depth = 0
    var description: String? = null
    var childRequired = false
    var extensionAllowed = false
    var takesValue = false
    var isNumeric = false
    var required = false
    var recommended = false
    var unitClass: String? = null

    init {
        name = xmlModel.name
        description = xmlModel.description
        childRequired = xmlModel.isChildRequired
        extensionAllowed = xmlModel.isExtensionAllowed
        takesValue = xmlModel.takesValue()
        isNumeric = xmlModel.isNumeric
        required = xmlModel.isRequired
        recommended = xmlModel.isRecommended
        unitClass = xmlModel.unitClass
    }

    /**
     * Tags are compared by their paths.
     */
    override fun compareTo(tagModel: TagModel): Int {
        return if (path == tagModel.path) 0 else -1
    }

    override fun toString(): String {
        return path
    }
}
