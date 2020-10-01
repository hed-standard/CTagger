import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class EventCodeList(codeSet: Set<String>, val tagger: CTagger) : JList<String>() {
    private val listModel = DefaultListModel<String>()
    private var prevSelected: String? = null

    init {
        codeSet.forEach{ listModel.addElement(it); tagger.finalMap[it] = "" }
        model = listModel
        addListSelectionListener(EventCodeListListener())
    }

    private class EventCodeListListener: ListSelectionListener {
        override fun valueChanged(e: ListSelectionEvent) {
            val eList = e.source as EventCodeList
            val tagger = eList.tagger
            val selected = eList.selectedValue
            var prevSelected = eList.prevSelected

            if (prevSelected != null)
                tagger.finalMap[prevSelected] = tagger.hedTagInput.text
            eList.prevSelected = selected

            if (tagger.finalMap.containsKey(selected))
                tagger.hedTagInput.text = tagger.finalMap[selected]
        }
    }
}
