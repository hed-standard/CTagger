import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList
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
                    println("code $selected selected")
                    var prevSelected = eList.prevSelected
                    println(tagger.fieldMap)
                    val codeMap = tagger.fieldMap[tagger.curField]

                    // save current tags
                    if (codeMap != null && prevSelected != null) // TODO
                        codeMap[prevSelected] = tagger.hedTagInput.text
                    eList.prevSelected = selected

                    // set hedTagInput to new text
                    if (codeMap != null && codeMap.containsKey(selected))
                        tagger.hedTagInput.text = codeMap[selected]
                }
            }
        }
    }
}
