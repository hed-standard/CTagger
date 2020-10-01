import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JTextPane
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException

class HedTagInput(val tagger: CTagger) : JTextPane() {
    var tagList: HedTagList? = null
        set(value) {
            field = value
            if (value != null) document.addDocumentListener(HedTagInputListener(this, value))
        }

    init{
//        addFocusListener(HedTagInputFocusListener())
    }

    fun replaceWordAtCaretWithTag(selectedTag: String) {
        try {
            val wordStartPos = findWordBeginning()
            val lastNode = selectedTag.split('/').last() // short-form tag
            select(wordStartPos, caretPosition) // prepare for next statement
            replaceSelection(lastNode)
        } catch (e: BadLocationException) {
            System.err.println(e)
        }
    }

    fun findWordBeginning(isInsert: Boolean = false): Int {
        var caretPos: Int
        if (isInsert) caretPos = caretPosition + 1 else caretPos = caretPosition - 1
        var wordStartPos = caretPos - 1
        if (wordStartPos > text.length) wordStartPos = -1
        while (wordStartPos > 0 && !getText(wordStartPos - 1, 1).matches(Regex(",|\\s|\\(|\\)|\\t|\\n|\\r"))) {
            --wordStartPos
        }
        return wordStartPos
    }
    private class HedTagInputListener(val tp: HedTagInput, val list: HedTagList) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) {
            val word = getWordAtCaret(true)
            if (word != null) list.showSearchResult(word)
        }

        override fun changedUpdate(e: DocumentEvent) {
        }

        override fun removeUpdate(e: DocumentEvent) {
            val word = getWordAtCaret(false)
            if (word != null) list.showSearchResult(word)
        }

        private fun getWordAtCaret(isInsert: Boolean): String? {
            try {
                val wordStartPos = tp.findWordBeginning(isInsert)
                if (tp.text.length > wordStartPos && wordStartPos > -1 && tp.caretPosition > wordStartPos) {
                    return tp.getText(wordStartPos, tp.caretPosition - wordStartPos)
                }
            } catch (e: BadLocationException) {
                System.err.println(e)
            }
            return null
        }

    }

    private class HedTagInputFocusListener : FocusListener {
        override fun focusGained(e: FocusEvent?) {
            return
        }
        override fun focusLost(e: FocusEvent?) {
            if (e != null) {
                val inputPane = e.source as HedTagInput
                val tagger = inputPane.tagger
                val selectedCode = tagger.eventCodeList.selectedValue // TODO temp
                tagger.finalMap[selectedCode] = inputPane.text
            }

        }
    }

}