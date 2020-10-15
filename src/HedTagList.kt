import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList

class HedTagList(val tagger: CTagger, val tags: List<String>, val hedInput: HedTagInput) : JList<String>() {
    private val listModel = DefaultListModel<String>()

    init{
        model = listModel
        tags.forEach{ listModel.addElement(it) }
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                val list = evt.source as JList<String>
                if (evt.clickCount == 2) {
                    // Double-click detected
//                            val index = list.locationToIndex(evt.point)
                    val selectedTag = list.selectedValue
                    hedInput.replaceWordAtCaretWithTag(selectedTag)
                }
            }
        })
    }

    // return size of the matchedTags list
    fun search(target: String) : Int{
        val matchedTags = tags.filter {
            // parse takeValues node if applicable
            val splitted = target.split('/')
            it.contains(target, true) || (splitted.size >= 2 && it.contains(splitted[splitted.size-2] + "/#", true))
        } // beautiful syntax comparing to Java!

        listModel.clear()
        matchedTags.forEach { listModel.addElement(it) }
        return matchedTags.size
    }

    fun show(x: Int, y: Int) {
        setLocation(x, y)
        isVisible = true
    }

}