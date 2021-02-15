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
                // save current work
                if (curField != null) {
                    val map = fieldMap[curField!!]
                    val key = tagger.eventCodeList.selectedValue
//                    eventCodeList.prevSelected = null
                    if (map != null && key != null)
                        map[key] = tagger.hedTagInput.getCleanHEDString()
                    tagger.hedTagInput.text = null
                }
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
    fun addFieldFromDict(field:String, fieldDict: CTagger.BIDSFieldDict) {
        addItem(field)
        // add unique codes to each field, ignoring BIDS default numerical fields
        if (listOf("duration", "onset", "sample", "stim_file", "hed", "response_time").contains(field)) {
            fieldAndUniqueCodeMap[field] = listOf("HED")
            isValueField[field] = true
        } else {
            if (fieldDict.Levels.isNotEmpty()) {
                fieldAndUniqueCodeMap[field] = fieldDict.Levels.keys.toList()
                isValueField[field] = false
            } else {
                fieldAndUniqueCodeMap[field] = listOf("HED")
                isValueField[field] = true
            }
        }
        // initialize fieldMap
        fieldMap[field] = HashMap()
        fieldAndUniqueCodeMap[field]!!.forEach { fieldMap[field]!![it] = "" }

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