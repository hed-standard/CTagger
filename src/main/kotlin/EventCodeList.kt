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
                if (e != null && e.clickCount == 1) {
                    val eList = e.source as EventCodeList
                    if (!eList.valueIsAdjusting) {
                        val tagger = eList.tagger
                        // creating new one
                        val curField = tagger.fieldList.selectedItem.toString()
                        val selected = eList.selectedValue
                        println("code $selected selected")

                        // create new HedTagInput pertaining to the curField-curCode pair
                        val hedTagInput = HedTagInput(tagger, curField, selected)

                        eList.prevSelected = selected

                        // hide search result pane
                        tagger.hideSearchResultPane()
                    }
                }
        }
    }
}
