import java.awt.event.*
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JScrollPane

class HedTagList(private val tagger: CTagger, private val tags: List<String>, private val hedInput: HedTagInput) : JList<String>() {
    private val listModel = DefaultListModel<String>()

    init{
        model = listModel
        tags.forEach{ listModel.addElement(it) }
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                if (evt.clickCount == 2) {
                    // Double-click detected
                    tagSelected()
                }
            }
        })
        addKeyListener(ListKeySelectListener(this, tagger))
        focusTraversalKeysEnabled = false
//        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "scrollDown")
//        actionMap.put("scrollDown", scroll("d"))
//        inputMap.put(KeyStroke.getKeyStroke("UP"), "scrollUp")
//        actionMap.put("scrollUp", scroll("u"))
//        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "tagSelected")
//        actionMap.put("tagSelected", tagSelected())
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

    fun tagSelected() {
        val selectedTag = selectedValue
        hedInput.replaceWordAtCaretWithTag(selectedTag)
        tagger.hideSearchResultPane()
    }

    class ListKeySelectListener(private val tagList: HedTagList, private val tagger: CTagger) : KeyListener {
        override fun keyTyped(e: KeyEvent?) {
        }

        override fun keyReleased(e: KeyEvent?) {
        }
        override fun keyPressed(e: KeyEvent?) {
            if (e != null) {
                when (e.keyCode) {
//                    KeyEvent.VK_DOWN -> tagList.selectedIndex
//                    KeyEvent.VK_UP -> tagList.selectedIndex
                    KeyEvent.VK_ENTER -> tagList.tagSelected()
                }
//                print("here")
                tagger.searchResultPanel.revalidate()
                tagger.searchResultPanel.repaint()
            }
        }
    }
}