import java.awt.Dimension
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class EventCodeList(val tagger: CTagger) : JList<String>() {
    private val listModel = DefaultListModel<String>()
    var prevSelected: String? = null
    var codeSet: List<String> = listOf()
        set(value) {
            field = value
            listModel.clear()
            updateListModel()
        }

    init {
        model = listModel
        addMouseListener(EventCodeListListener(tagger))
    }

    private fun updateListModel() {
        if (listModel.isEmpty)
            codeSet.forEach{ listModel.addElement(it) }
    }
    private class EventCodeListListener(val tagger:CTagger): MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            // TODO fix click event called twice issue
                if (e != null && e.clickCount == 1) {
                    val eList = e.source as EventCodeList
                    if (!eList.valueIsAdjusting) {
                        val tagger = eList.tagger
                        val selected = eList.selectedValue
                        var prevSelected = eList.prevSelected
                        println("code $selected selected")
                        // Check for invalid tag. Only proceed if no invalid tags found
//                    val invalidTags = tagger.hedTagInput.findInvalidTags()
//                    if (invalidTags.isNotEmpty()) {
//                        JOptionPane.showMessageDialog(tagger.frame,
//                                "Please fix invalid tags (in red)",
//                                "Invalid tags found",
//                                JOptionPane.ERROR_MESSAGE);
//                        eList.setSelectedValue(prevSelected, true)
//                    }
//                    else {
                        // creating new one
                        val curField = tagger.fieldList.selectedItem.toString()
                        val codeMap = tagger.fieldList.fieldMap[curField]

                        // create new HedTagInput pertaining to the curField-curCode pair
                        val hedTagInput = HedTagInput(tagger, curField, selected)

                        // set hedTagInput to new text
                        if (codeMap != null && codeMap.containsKey(selected))
                            tagger.inputPane.resume(codeMap[selected])
                        // save current tags
//                        if (codeMap != null && prevSelected != null) {
//                            codeMap[prevSelected] = tagger.hedTagInput.getCleanHEDString()
//                        }

                        eList.prevSelected = selected


                        // hide search result pane
                        tagger.hideSearchResultPane()
                    }
                }
        }
    }
}
