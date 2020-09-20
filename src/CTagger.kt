import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.io.StringReader
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.text.BadLocationException
import javax.swing.text.JTextComponent
import javax.xml.bind.JAXBContext


val tags: MutableList<String> = mutableListOf()

fun main() {
    getHedXmlModel()
    val app = App()
}

class App {
    constructor() {
        val frame = JFrame("CTAGGER IDE version")
        frame.size = Dimension(800, 800)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        frame.setLocation(dim.width / 2 - frame.size.width / 2, dim.height / 2 - frame.size.height / 2)

        val mainPane = JPanel(BorderLayout())
        val hedTagInput = JTextArea(30, 30)
        val searchPanel = JPanel()
        searchPanel.setSize(mainPane.width, mainPane.height)
        hedTagInput.document.addDocumentListener(HedTagInputListener(hedTagInput, searchPanel, hedTagInput))

        mainPane.add(hedTagInput, BorderLayout.CENTER)
        mainPane.add(searchPanel, BorderLayout.SOUTH)

        frame.contentPane.add(mainPane)

        frame.pack()
        frame.repaint()
        frame.isVisible = true
    }

    class HedTagInputListener(val tc: JTextArea, val panel: JPanel, val editor: JTextArea) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) {
//            print("insert. ")
//            println("length: ${tc.text.length}, caret: ${tc.caretPosition}")
            val word = getWordAtCaret(true)
            if (word != null) showSearchResult(searchTags(word))
        }
        override fun changedUpdate(e: DocumentEvent) {

        }

        override fun removeUpdate(e: DocumentEvent) {
//            print("remove. ")
//            println("length: ${tc.text.length}, caret: ${tc.caretPosition}")
            val word = getWordAtCaret(false)
            if (word != null) showSearchResult(searchTags(word))
        }

        private fun getWordAtCaret(isInsert: Boolean): String? {
            try {
                val wordStartPos = findBeginning(isInsert)
                if (tc.text.length > wordStartPos && wordStartPos > -1 && tc.caretPosition > wordStartPos) {
                    val word = tc.getText(wordStartPos, tc.caretPosition - wordStartPos)
                    return word
                }
            } catch (e: BadLocationException) {
                System.err.println(e)
            }
            return null
        }
        private fun searchTags(input: String): List<String> {
            // TODO optimize
            return tags.filter{ it.contains(input, true)} // beautiful syntax comparing to Java!
        }

        private fun showSearchResult(matchedTags: List<String>) {
            val list = JList<String>(matchedTags.toTypedArray())
            list.addListSelectionListener {
                if (!it.valueIsAdjusting) {
                    val selectedTag = list.selectedValue
                    replaceWithTag(selectedTag)
                }
            }
            val scrollPane = JScrollPane(list)
            scrollPane.setSize(panel.width, panel.height)
            panel.removeAll()
            panel.add(scrollPane)
        }

        private fun replaceWithTag(selectedTag: String) {
            try {
                var wordStartPos = findBeginning()
                val lastNode = selectedTag.split('/').last()
                tc.replaceRange(null,wordStartPos, tc.caretPosition)
                tc.insert(lastNode, wordStartPos)
            } catch (e: BadLocationException) {
                System.err.println(e)
            }
        }

        private fun findBeginning(isInsert: Boolean = false): Int {
            var caretPosition: Int
            if (isInsert) caretPosition = tc.caretPosition + 1 else caretPosition = tc.caretPosition - 1
            var wordStartPos = caretPosition - 1
            if (wordStartPos > tc.text.length) wordStartPos = -1
//            println("length: ${tc.text.length}, startPos: $wordStartPos, caret: ${tc.caretPosition}")
            while (wordStartPos > 0 && !tc.getText(wordStartPos-1, 1).matches(Regex(",|\\s|\\(|\\)|\\t|\\n|\\r"))) {
                --wordStartPos
            }
            return wordStartPos
        }
    }
}

fun getHedXmlModel() {
    val xmlData = TestUtilities.getResourceAsString(TestUtilities.HedFileName)
    val hedXmlModel: HedXmlModel
    try {
        val context = JAXBContext.newInstance(HedXmlModel::class.java)
        hedXmlModel = context.createUnmarshaller().unmarshal(StringReader(xmlData)) as HedXmlModel
        println(hedXmlModel.version)
    }
    catch(e: Exception) {
        throw RuntimeException("Unable to read XML data: " + e.message)
    }
    populateTagSets("", hedXmlModel.tags)
}

fun populateTagSets(path: String, tagSets: Set<TagXmlModel>) {
    for (tagXmlModel: TagXmlModel in tagSets) {
        tags.add(path + tagXmlModel.name)
        populateTagSets(path + tagXmlModel.name + "/", tagXmlModel.tags)
    }
}
