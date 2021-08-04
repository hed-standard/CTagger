import java.awt.event.*
import javax.swing.DefaultListModel
import javax.swing.JList

/**
 * Show search result
 */
class SearchResultTagList(private val tagger: CTagger, tags: List<String>, private val hedInput: HedTagInput) : JList<String>(), FocusListener {
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
        addFocusListener(this)
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
    fun addTagsToList(tags: List<String>) {
        listModel.clear()
        tags.forEach { listModel.addElement(it) }
    }

    fun tagSelected() {
        val selectedTag = selectedValue
        hedInput.replaceWordAtCaretWithTag(selectedTag)
        tagger.inputPane.hideSearchResultPane()
    }

    override fun focusGained(e: FocusEvent?) {
        return
    }

    override fun focusLost(e: FocusEvent?) {
        tagger.inputPane.hideSearchResultPane()
    }

    fun isEmpty() : Boolean{
        return listModel.isEmpty
    }

    /**
     * Clear list
     */
    fun clear() {
        listModel.clear()
    }
    class ListKeySelectListener(private val tagList: SearchResultTagList, private val tagger: CTagger) : KeyListener {
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
                tagger.inputPane.searchResultPanel.revalidate()
                tagger.inputPane.searchResultPanel.repaint()
            }
        }
    }
}