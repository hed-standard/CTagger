import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.StreamTokenizer
import java.io.StringReader
import java.text.BreakIterator
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.Utilities
import kotlin.text.Regex

class HedTagInput(private val tagger: CTagger) : JTextPane(), DocumentListener, KeyListener, MouseListener {
    private var isReplace = false
    private val validTagPattern = "\\w|\\+|\\^|-|\\s|\\d|/"
    private var caretPos = 0
    init {
        document.addDocumentListener(this)
        addKeyListener(this)
        addMouseListener(this)
    }

    /**
     * Action handler when a character is inserted
     * 1. Get word (HED tag) to which the new character is part of
     * 2. Search and process result
     *      2.1. If tag exist, show search result in the window below the word, black highlight (overwritting any active red highlight).
     *      2.2. If tag doesn't exist, hide result window (if on) and red highlight the word
     */
    override fun insertUpdate(e: DocumentEvent) {
//        val result = Pair<Int,Int>(Utilities.getWordStart(this,caretPosition), Utilities.getWordEnd(this, caretPosition))
        caretPos = caretPosition
        val result = getTagAtPos(caretPosition)

        if (result != null) {
            val isValid = tagger.hedValidator.validateEntry(text.substring(result.first, result.second))
            if (isValid) {
                try {
                    tagger.showSearchResultPane(x + 5, y + 25) // put the search result at the left most but under current caret
                }
                catch (e: Exception) {
                    print("Exception " + e.message)
                    e.printStackTrace()
                }
//                blackHighlight(result.first, result.second)
//                requestFocusInWindow()
            }
            else {
                tagger.hideSearchResultPane()
//                redHighlight(result.first, result.second)
            }
        }

    }

    override fun changedUpdate(e: DocumentEvent) {
    }

    override fun removeUpdate(e: DocumentEvent) {
//        if (!isReplace && text.isNotEmpty()) {
//            var pos = caretPosition - e.length
//            if (pos >= text.length) pos = text.length-1
//            val result = getWordAtPos(pos)
//            if (result != null) {
//                tagger.hideSearchResultPane()
////                val isValid = tagger.hedValidator.validateEntry(text.substring(result.first, result.second))
////                if (!isValid) redHighlight(result.first, result.second) else blackHighlight(result.first, result.second)
//            }
//        }
    }

    // black highlight compatible input
    private fun blackHighlight(start:Int, end:Int) {
        SwingUtilities.invokeLater {
            val attrs = SimpleAttributeSet()
            StyleConstants.setForeground(attrs, Color.BLACK)
            val doc = this.styledDocument
            doc.setCharacterAttributes(start, end-start, attrs, false)
            //reset
            doc.setCharacterAttributes(end, 1, this.characterAttributes, true)
        }
    }

    // red highlight incompatible input
    private fun redHighlight(start:Int, end: Int) {
        SwingUtilities.invokeLater {
            val attrs = SimpleAttributeSet()
            StyleConstants.setForeground(attrs, Color.RED)
            val doc = this.styledDocument
            doc.setCharacterAttributes(start, end-start, attrs, false)
            //reset
            doc.setCharacterAttributes(end, 1, this.characterAttributes, true)
        }
    }

    private fun getTagAtPos(pos:Int): Pair<Int,Int>? {
        try {
            var startPos = Utilities.getWordStart(this,pos)
            while ((startPos > 0 && text[startPos] == '/') || (startPos > 1 && text[startPos-1] == '/')) {
                var newPos = if (text[startPos] == '/') startPos else startPos-1
                startPos = Utilities.getWordStart(this, newPos-1)
            }
            var endPos = pos
//            println(text.substring(startPos,endPos))
            if (text.substring(startPos,endPos) == ",")
                return null
            else
                return Pair(startPos, pos)
        } catch (e: BadLocationException) {
            System.err.println(e)
            return null
        }
    }

    fun replaceWordAtCaretWithTag(selectedTag: String) {
        try {
            val result = getTagAtPos(caretPos)//findWordBeginning(caretPosition)
            if (result != null) {
                val nodes = selectedTag.split('/') // short-form tag
                select(result.first, result.second) // prepare for next statement
                isReplace = true // tell removeUpdate to ignore
                if (nodes.last() == "#") replaceSelection(nodes[nodes.size - 2] + "/") else replaceSelection(nodes.last()) //TODO need to account for whether takeValues is enforced
                isReplace = false // replace done. Reset
                grabFocus()
            }
        } catch (e: BadLocationException) {
            System.err.println(e)
        }
    }

    override fun keyReleased(e: KeyEvent?) {
    }

    override fun keyTyped(e: KeyEvent?) {
    }
    override fun keyPressed(e: KeyEvent?) {
        if (e != null && e.keyCode == KeyEvent.VK_DOWN && tagger.searchResultPanel.isVisible) {
            tagger.hedTagList.requestFocusInWindow()
            tagger.hedTagList.selectedIndex = 0
            tagger.searchResultPanel.revalidate()
            tagger.searchResultPanel.repaint()
        }
        else if (e != null && (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_ESCAPE) && tagger.searchResultPanel.isVisible) {
            tagger.hideSearchResultPane()
        }
    }

    override fun mousePressed(e: MouseEvent) {
    }

    override fun mouseReleased(e: MouseEvent) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }

    override fun mouseClicked(e: MouseEvent) {
        if (e != null && tagger.searchResultPanel.isVisible) {
            tagger.hideSearchResultPane()
        }
    }

    fun resume(s: String?) {
        if (s != null) {
            isReplace = true
            text = s
            isReplace = false
        }
    }

    fun getCleanHEDString(): String {
        return text.trim().trim(',')
    }
}