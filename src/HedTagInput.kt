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
import kotlin.text.Regex

class HedTagInput(private val tagger: CTagger) : JTextPane(), DocumentListener, KeyListener, MouseListener {
    private var isReplace = false
    private val validTagPattern = "\\w|\\+|\\^|-|\\d|/"
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
        val result = getWordAtPos(caretPosition)
        if (result != null) {
            val numResults = checkValidity(text.substring(result.first, result.second))
            if (numResults > 0) {
                try {
                    tagger.showSearchResultPane(x + 5, this.caret.magicCaretPosition.y + 25) // put the search result at the left most but under current caret
                }
                catch (e: Exception) {

                }
                blackHighlight(result.first, result.second)
//                requestFocusInWindow()
            } else {
                tagger.hideSearchResultPane()
                redHighlight(result.first, result.second)
            }
        }
    }

    override fun changedUpdate(e: DocumentEvent) {
    }

    override fun removeUpdate(e: DocumentEvent) {
        if (!isReplace && text.isNotEmpty()) {
            var pos = caretPosition - e.length
            if (pos >= text.length) pos = text.length-1
            val result = getWordAtPos(pos)
            if (result != null) {
                val numResults = tagger.hedTagList.search(text.substring(result.first, result.second))
                if (numResults == 0) redHighlight(result.first, result.second) else blackHighlight(result.first, result.second)
            }
        }
    }

    /**
     * This function is sensitive to the schema specification
     */
    fun checkValidity(tag:String): Int {
        val splitted = tag.split('/')
        if (splitted.size >= 2) {
            if (!tagger.schema.containsKey(splitted[splitted.size-1])) {
                if (tagger.schema.containsKey(splitted[splitted.size-2])) {
                    // Could be one of these scenarios:
                    // 1. Value of takesValue node --> prev node is requireChild
                    // 2. Extension of extensionAllowed node
                    // 3. Unfinished node typing (e.g. Event/Se) --> previous node is not end node
                    // check based on the previous node
                    val tagModel = tagger.schema[splitted[splitted.size-2]]!!
                    if (tagModel.childRequired) {
                        // validate takesValue input
                        val valueNode = tagger.schema["${splitted[splitted.size-2]}/#"] // all childRequired nodes are followed by a takesValue node
                        return 1
                    }
                    else if (tagModel.extensionAllowed) {
                        return 1
                    }
                    else {
                        // unfinished node typing --> show suggestion
                        return tagger.hedTagList.search(tag)
                    }
                }
            }
            // last node exists in schema. Check if full path is valid
            return if (tagger.schema.containsKey(tag)) 1 else 0
        }
        else
            return tagger.hedTagList.search(tag)
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

    private fun getWordAtPos(pos:Int): Pair<Int,Int>? {
        try {
            // start with the last character. If beginning of doc, set to -1
            var startPos = if (pos >= -1) pos else -1

            // backtrack until an invalid character or end of text
            val regex = Regex(validTagPattern)
//            println(text)
            while (startPos >= 0 && regex.matches(text[startPos].toString())) {
                startPos--
            }
            // startPos is now at the invalid character or -1. Bring it forward to the correct location
            startPos++

            var endPos = startPos
            // increase endPos until end of text or first invalid character
            while (endPos < text.length && regex.matches(text[endPos].toString())) {
                endPos++
            }
//            println(startPos)
//            println(endPos)
            return if (text.substring(startPos, endPos).isNullOrEmpty()) null else Pair(startPos, endPos) //text.substring(startPos,endPos) // substring stops at endPos-1
        } catch (e: BadLocationException) {
            System.err.println(e)
        }
        return null
    }

    fun replaceWordAtCaretWithTag(selectedTag: String) {
        try {
            val result = getWordAtPos(caretPosition-1)//findWordBeginning(caretPosition)
            if (result != null) {
                val nodes = selectedTag.split('/') // short-form tag
                select(result.first, result.second) // prepare for next statement
                isReplace = true // tell removeUpdate to ignore
                if (nodes.last() == "#") replaceSelection(nodes[nodes.size - 2] + "/") else replaceSelection(nodes.last() + ", ") //TODO need to account for whether takeValues is enforced
                isReplace = false // replace done. Reset
                grabFocus()
            }
        } catch (e: BadLocationException) {
            System.err.println(e)
        }
    }

    fun findInvalidTags(): List<String> {
        val invalidTags = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val loc = getWordAtPos(i)
            if (loc != null) {
                val tag = text.substring(loc.first, loc.second)
                if (checkValidity(tag) == 0) {
                    invalidTags.add(tag)
                }
                // advance to beginning of next word
                val regex = Regex(validTagPattern)
                i = loc.second
                while (i < text.length && !regex.matches(text[i].toString())) {
                    i++
                }
            }
            else {
                i++
            }
        }
        return invalidTags
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
        else if (e != null && e.keyCode == KeyEvent.VK_ENTER && tagger.searchResultPanel.isVisible) {
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