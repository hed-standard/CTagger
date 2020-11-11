import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class HedTagInput(val tagger: CTagger) : JTextPane(), DocumentListener, KeyListener, MouseListener {
    init {
        document.addDocumentListener(this)
        addKeyListener(this)
        addMouseListener(this)
    }
    /* Note on how indexing and caret position works */
    /*
        Think of text as a character array. Thus text.charAt(caretPosition) get the character to the right of the caret.
        When insertUpdate and removeUpdate is entered, the caretPosition is still at last flashed. Thus it's not the final position when the update completes
     */
    fun replaceWordAtCaretWithTag(selectedTag: String) {
        try {
            val wordStartPos = findWordBeginning(caretPosition)
            val nodes = selectedTag.split('/') // short-form tag
            select(wordStartPos, caretPosition) // prepare for next statement
            if (nodes.last() == "#") replaceSelection(nodes[nodes.size-2] + "/") else replaceSelection(nodes.last() + ", ") //TODO need to account for whether takeValues is enforced
            grabFocus()
        } catch (e: BadLocationException) {
            System.err.println(e)
        }
    }

    fun findWordBeginning(endPos: Int): Int {
        var wordStartPos = endPos - 1
        if (wordStartPos > text.length) wordStartPos = -1
        while (wordStartPos > 0 && !getText(wordStartPos - 1, 1).matches(Regex(",|\\s|\\(|\\)|\\t|\\n|\\r"))) {
            --wordStartPos
        }
        return wordStartPos
    }
//    private class HedTagInputListener(val tp: HedTagInput, val list: HedTagList) : DocumentListener {
    /**
     * Action handler when a character is inserted
     * 1. Get word to which the new character is part of
     * 2. Search and process result
     *      2.1. If tag exist, show search result in the window below the word, black highlight (overwritting any active red highlight).
     *      2.2. If tag doesn't exist, hide result window (if on) and red highlight the word
     */
    override fun insertUpdate(e: DocumentEvent) {
        val word = getWordAtCaret(true)
        if (word != null) {
            val numResults = tagger.hedTagList.search(word)
            if (numResults > 0) {
                tagger.showSearchResultPane(x+5, caret.magicCaretPosition.y+25) // put the search result at the left most but under current caret
                blackHighlight(word)
//                requestFocusInWindow()
            } else {
                tagger.hideSearchResultPane()
                redHighlight(word)
            }
        }
    }

    override fun changedUpdate(e: DocumentEvent) {
    }

    override fun removeUpdate(e: DocumentEvent) {
        val word = getWordAtCaret(false)
        if (word != null) {
            val numResults = tagger.hedTagList.search(word)
            if (numResults == 0) redHighlight(word) else blackHighlight(word)
        }
    }

    // black highlight compatible input
    private fun blackHighlight(word: String) {
        SwingUtilities.invokeLater {
            val attrs = SimpleAttributeSet()
            StyleConstants.setForeground(attrs, Color.BLACK)
            val doc = this.styledDocument
            doc.setCharacterAttributes(this.caretPosition - word.length, word.length, attrs, false)
            //reset
            doc.setCharacterAttributes(this.caretPosition, 1, this.characterAttributes, true)
        }
    }

    // red highlight incompatible input
    private fun redHighlight(word: String) {
        SwingUtilities.invokeLater {
            val attrs = SimpleAttributeSet()
            StyleConstants.setForeground(attrs, Color.RED)
            val doc = this.styledDocument
            doc.setCharacterAttributes(this.caretPosition - word.length, word.length, attrs, false)
            //reset
            doc.setCharacterAttributes(this.caretPosition, 1, this.characterAttributes, true)
        }
    }

    private fun getWordAtCaret(isInsert: Boolean): String? {
        try {
            var endPos = if (isInsert) this.caretPosition + 1 else this.caretPosition - 1
            if (this.text.endsWith(',')) --endPos
            val wordStartPos = this.findWordBeginning(endPos)
            if (this.text.length > wordStartPos && wordStartPos > -1 && this.caretPosition > wordStartPos) {
                return this.getText(wordStartPos, endPos - wordStartPos)
            }
        } catch (e: BadLocationException) {
            System.err.println(e)
        }
        return null
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
}