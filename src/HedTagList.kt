import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList

class HedTagList(val tags: List<String>) : JList<String>() {
    private val listModel = DefaultListModel<String>()
    var hedInput: HedTagInput? = null
        set(value) {
            field = value
            if (value != null) {
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(evt: MouseEvent) {
                        val list = evt.source as JList<String>
                        if (evt.clickCount == 2) {
                            // Double-click detected
//                            val index = list.locationToIndex(evt.point)
                            val selectedTag = list.selectedValue
                            value.replaceWordAtCaretWithTag(selectedTag)
                        }
                    }
                })
            }
        }

    init{
        model = listModel
        hedInput = null
    }

    fun showSearchResult(target: String) {
        val matchedTags = searchTags(target)
        listModel.clear()
        matchedTags.forEach { listModel.addElement(it) }
    }

    private fun searchTags(input: String): List<String> {
        // TODO optimize
        return tags.filter { it.contains(input, true) } // beautiful syntax comparing to Java!
    }
}