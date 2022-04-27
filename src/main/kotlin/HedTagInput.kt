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
import javax.swing.text.Utilities
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class HedTagInput(private val tagger: CTagger, private val curField: String, private val curCode: String) : JTextPane(), DocumentListener, KeyListener, MouseListener {
    private var needParsing = true
//    private val validTagPattern = "\\w|\\+|\\^|-|\\s|\\d|/"
    private var caretPos = 0
    private val defaultMessage = "Select field level and start tagging by typing here or click on \"Show HED schema\""
    private var job: CoroutineContext.Element? = null
    init {
        document.addDocumentListener(this)
        addKeyListener(this)
        addMouseListener(this)

        // set default string to display
        val resumeString = tagger.getHedString(curField, curCode)
        if (resumeString.isNotEmpty()) {
            resume(tagger.getHedString(curField, curCode))
        }
        else {
            val prevAttribute = characterAttributes
            val attributeSet = SimpleAttributeSet()
            StyleConstants.setItalic(attributeSet, true)
            setCharacterAttributes(attributeSet, true)
            resume(defaultMessage)
            setCharacterAttributes(prevAttribute, true)
        }
        tagger.inputPane.newTagInput(this)
        GlobalScope.async {
            autosave()
        }
    }

    /**
     * Action handler when a character is inserted
     * 1. Get word (HED tag) to which the new character is part of
     * 2. Search and process result
     *      2.1. If tag exist, show search result in the window below the word, black highlight (overwritting any active red highlight).
     *      2.2. If tag doesn't exist, hide result window (if on) and red highlight the word
     */
    override fun insertUpdate(e: DocumentEvent) {
        if (needParsing) {
            tagger.isValidated = false
            caretPos = caretPosition
            val result = getTagAtPos(caretPosition)

            if (result != null) {
                val enteredText = text.substring(result.first, result.second)
                if (!enteredText.trim().isEmpty()) {
                    findMatchingTags(enteredText)
                    if (!tagger.isSearchResultEmpty()) {
                        try {
                            val pos = modelToView(caretPosition)
                            tagger.showSearchResultPane(10, pos.y + 25) // put the search result at the left most but under current caret
                        } catch (e: Exception) {
                            print("Exception " + e.message)
                            e.printStackTrace()
                        }
                    } else {
                        tagger.hideSearchResultPane()
                    }
                } else {
                    tagger.hideSearchResultPane()
                }
            } else
                tagger.hideSearchResultPane()
        }
    }

    override fun changedUpdate(e: DocumentEvent) {
    }

    override fun removeUpdate(e: DocumentEvent) {
        if (needParsing) {
            tagger.isValidated = false
        }
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

    /**
     * Return the start and end location of the tag in the input pane
     */
    private fun getTagAtPos(pos:Int): Pair<Int,Int>? {
        try {
            var startPos = Utilities.getWordStart(this,pos)
            while ((startPos > 0 && startPos < text.length && text[startPos] == '/') || (startPos > 1 && text[startPos-1] == '/')) {
                val newPos = if (text[startPos] == '/') startPos else startPos-1
                startPos = Utilities.getWordStart(this, newPos-1)
            }
            val endPos = Utilities.getWordEnd(this,startPos)

            // if new line OR comma OR empty space
            if (startPos >= text.length || (startPos == endPos && text[startPos] == ',') || ((endPos-startPos) == 1 && text[startPos] == ' '))
                return null
            else
                return Pair(startPos, endPos)
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
                needParsing = false // tell removeUpdate to ignore
                if (nodes.last() == "#") replaceSelection(nodes[nodes.size - 2] + "/") else replaceSelection(nodes.last()) //TODO need to account for whether takeValues is enforced
                needParsing = true // replace done. Reset
                grabFocus()
            }
        } catch (e: BadLocationException) {
            System.err.println(e)
        }
    }

    private fun findMatchingTags(entry: String) {
        val tags = tagger.tags
        val matchedTags = tags.filter {
//            // parse takeValues node if applicable
//            val splitted = target.split('/')
            it.contains(entry, true)// || (splitted.size >= 2 && it.contains(splitted[splitted.size-2] + "/#", true))
        } // beautiful syntax comparing to Java!
        if (matchedTags.isNotEmpty()) {
            tagger.addTagsToSearchResult(matchedTags)
        }
    }

    override fun keyReleased(e: KeyEvent?) {
    }

    override fun keyTyped(e: KeyEvent?) {
    }
    override fun keyPressed(e: KeyEvent?) {
        if (e != null && e.keyCode == KeyEvent.VK_DOWN && tagger.isSearchResultVisible()) {
            tagger.showSearchResultWhenKeyPressed()
        }
        else if (e != null && (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_ESCAPE) && tagger.isSearchResultVisible()) {
            tagger.hideSearchResultPane()
        }
    }

    override fun mousePressed(e: MouseEvent) {
        if (text == defaultMessage) {
            needParsing = false
            text = ""
            needParsing = true
        }
    }

    override fun mouseReleased(e: MouseEvent) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }

    override fun mouseClicked(e: MouseEvent) {
        if (e != null && tagger.isSearchResultVisible()) {
            tagger.hideSearchResultPane()
        }
    }

    fun resume(s: String?) {
        if (s != null) {
            needParsing = false
            text = s
            needParsing = true
        }
    }

    fun getCleanHEDString(): String {
        var newstring = text.replace("\n", "")
        newstring = newstring.replace("\t", "")
        return newstring.trim().trim(',')
    }

    fun setNoParsing() {
        needParsing = false
    }

    fun setParsing() {
        needParsing = true
    }

    private suspend fun autosave() = coroutineScope {
        job = launch {
//            print(this.toString())
            while (true) {
                delay(100L)
                try {
                    val fieldMap = tagger.fieldList.fieldMap
                    if (curField != null && fieldMap.containsKey(curField)) {
                        val codeMap = fieldMap[curField]
                        if (curCode != null && codeMap!!.containsKey(curCode) && text != defaultMessage) {
                            codeMap!![curCode] = getCleanHEDString()
                        }
                    }
                }
                catch (e: Exception) {
                    // Simply ignore and not save
                }
            }
        }
    }

    fun cancelAutosave() {
//        println("Canceling $job")
        job?.cancel()
    }
}