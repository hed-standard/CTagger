import java.awt.event.ItemEvent
import javax.swing.JComboBox

class FieldList(val tagger: CTagger): JComboBox<String>() {
    val fieldMap = HashMap<String, HashMap<String,String>>()
    var fieldAndUniqueCodeMap = HashMap<String, List<String>>()
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
}