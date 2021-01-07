import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

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
        addMouseListener(EventCodeListListener())
    }

    private fun updateListModel() {
        if (listModel.isEmpty)
            codeSet.forEach{ listModel.addElement(it) }
    }
    private class EventCodeListListener: MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            super.mouseClicked(e)
            if (e != null) {
                val eList = e.source as EventCodeList
                if (!eList.valueIsAdjusting) {
                    val tagger = eList.tagger
                    val selected = eList.selectedValue
                    var prevSelected = eList.prevSelected
                    println("code $selected selected")
                    println(tagger.fieldMap)

                    // Check for invalid tag. Only proceed if no invalid tags found
                    val invalidTags = tagger.hedTagInput.findInvalidTags()
                    if (invalidTags.isNotEmpty()) {
                        JOptionPane.showMessageDialog(tagger.frame,
                                "Please fix invalid tags (in red)",
                                "Invalid tags found",
                                JOptionPane.ERROR_MESSAGE);
                        eList.setSelectedValue(prevSelected, true)
                    }
                    else {
                        // save current tags
                        val curField = tagger.fieldCB.selectedItem.toString()
                        val codeMap = tagger.fieldMap[curField]
                        if (codeMap != null && prevSelected != null) {
                            codeMap[prevSelected] = tagger.hedTagInput.getCleanHEDString()
                        }

                        eList.prevSelected = selected

                        // set hedTagInput to new text
                        if (codeMap != null && codeMap.containsKey(selected))
                            tagger.hedTagInput.resume(codeMap[selected])

                        // hide search result pane
                        tagger.hideSearchResultPane()
                    }
                }
            }
        }
    }
}
