import java.awt.Color
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class HedTagInput(val tagger: CTagger) : JTextPane() {
    var tagList: HedTagList? = null
        set(value) {
            field = value
            if (value != null) document.addDocumentListener(HedTagInputListener(this, value))
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
            if (nodes.last() == "#") replaceSelection(nodes[nodes.size-2] + "/") else replaceSelection(nodes.last()) //TODO need to account for whether takeValues is enforced
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
    private class HedTagInputListener(val tp: HedTagInput, val list: HedTagList) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) {
            val word = getWordAtCaret(true)
            if (word != null) {
                val numResults = list.showSearchResult(word)
                if (numResults == 0) redHighlight(word) else blackHighlight(word)
            }
        }

        override fun changedUpdate(e: DocumentEvent) {
        }

        override fun removeUpdate(e: DocumentEvent) {
            val word = getWordAtCaret(false)
            if (word != null) {
                val numResults = list.showSearchResult(word)
                if (numResults == 0) redHighlight(word) else blackHighlight(word)
            }
        }

        // black highlight compatible input
        private fun blackHighlight(word: String) {
            SwingUtilities.invokeLater {
                val attrs = SimpleAttributeSet()
                StyleConstants.setForeground(attrs, Color.BLACK)
                val doc = tp.styledDocument
                doc.setCharacterAttributes(tp.caretPosition - word.length, word.length, attrs, false)
                //reset
                doc.setCharacterAttributes(tp.caretPosition, 1, tp.characterAttributes, true)
            }
        }

        // red highlight incompatible input
        private fun redHighlight(word: String) {
            SwingUtilities.invokeLater {
                val attrs = SimpleAttributeSet()
                StyleConstants.setForeground(attrs, Color.RED)
                val doc = tp.styledDocument
                doc.setCharacterAttributes(tp.caretPosition - word.length, word.length, attrs, false)
                //reset
                doc.setCharacterAttributes(tp.caretPosition, 1, tp.characterAttributes, true)
            }
        }

        private fun getWordAtCaret(isInsert: Boolean): String? {
            try {
                var endPos = if (isInsert) tp.caretPosition + 1 else tp.caretPosition - 1
                if (tp.text.endsWith(',')) --endPos
                val wordStartPos = tp.findWordBeginning(endPos)
                if (tp.text.length > wordStartPos && wordStartPos > -1 && tp.caretPosition > wordStartPos) {
                    return tp.getText(wordStartPos, endPos - wordStartPos)
                }
            } catch (e: BadLocationException) {
                System.err.println(e)
            }
            return null
        }

    }

}