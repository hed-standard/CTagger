import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Rectangle
import javax.swing.JLayeredPane
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

class InputLayeredPane(private val tagger:CTagger) : JLayeredPane() {
    var inputScrollPane: JScrollPane = JScrollPane()
    var searchResultPanel: JScrollPane = JScrollPane()
    var searchResultTagList: SearchResultTagList? = null
    var hedTagInput: HedTagInput? = null
    init {
        layout = FlowLayout()
    }

    fun newTagInput(tagInput: HedTagInput) {
        hedTagInput?.cancelAutosave()

        hedTagInput = tagInput
        // remove old GUI. Will garbage collector automatically handle them?
        remove(inputScrollPane)
        remove(searchResultPanel)

        // add new HedTagInput to GUI
        SwingUtilities.invokeLater {
            hedTagInput!!.preferredSize = Dimension(500,300)
            inputScrollPane = JScrollPane(hedTagInput)
            inputScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER // Force wrapping. Deduced from: http://java-sl.com/wrap.html
            inputScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            inputScrollPane.bounds = Rectangle(10,0,hedTagInput!!.preferredSize.width, hedTagInput!!.preferredSize.height)
            add(inputScrollPane, Integer(0), 1)

            searchResultTagList = SearchResultTagList(tagger, tagger.tags, hedTagInput!!)
            searchResultPanel = JScrollPane(searchResultTagList)
            searchResultPanel.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            searchResultPanel.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            searchResultPanel.setBounds(10, 10, 480,150)
//        searchResultPanel.location = Point(30,150)
            searchResultPanel.isVisible = false
            add(searchResultPanel, Integer(0), 0)
        }
    }

    fun hideSearchResultPane() {
        SwingUtilities.invokeLater {
            searchResultPanel.isVisible = false
            remove(searchResultPanel)
            repaint()
        }
    }

    fun showSearchResultPane(x: Int, y: Int) {
        SwingUtilities.invokeLater {
            searchResultPanel.bounds = Rectangle(x+5, y, 480,150)
            searchResultPanel.isVisible = true
            searchResultPanel.revalidate()
            searchResultPanel.repaint()
            add(searchResultPanel, Integer(0),0)
            repaint()
        }
    }

    fun showSearchResultWhenDownKeyPressed() {
        if (searchResultTagList != null) {
            searchResultTagList!!.requestFocusInWindow()
            searchResultTagList!!.selectedIndex = 0
            searchResultPanel.revalidate()
            searchResultPanel.repaint()
        }
    }
    fun isSearchResultEmpty(): Boolean {
        // using Elvis operator
        return searchResultTagList?.isEmpty() ?: true
    }

    fun addTagsToSearchResult(matchedTags: List<String>) {
        searchResultTagList?.addTagsToList(matchedTags)
    }

    fun isSearchResultVisible(): Boolean{
        return searchResultPanel.isVisible
    }

    fun clearSearchResult() {
        searchResultTagList?.clear()
    }

    fun resume(s:String?) {
        if (s != null)
            hedTagInput?.resume(s)
    }

    fun insertTagAtCaret(tag:String) {
        val hedInputDoc = hedTagInput?.document
        if (hedInputDoc != null && hedTagInput != null)
            hedInputDoc.insertString(hedTagInput!!.caretPosition, tag, null)
    }

    fun getCleanedHEDString(): String{
        if (hedTagInput != null)
            return hedTagInput!!.getCleanHEDString()
        else
            return ""
    }

}