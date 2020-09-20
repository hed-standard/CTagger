import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.StringReader
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.xml.bind.JAXBContext


val tags: MutableList<String> = mutableListOf()

fun main() {
    getHedXmlModel()
    SwingUtilities.invokeLater { App() }
}

class App {
    constructor() {
        val frame = JFrame("CTAGGER IDE version")
        frame.setSize(1024, 800)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        frame.setLocation(dim.width / 2 - frame.size.width / 2, dim.height / 2 - frame.size.height / 2)
        val BLUE_MEDIUM = Color(168, 194, 255)

        val mainPane = frame.contentPane
        mainPane.layout = BorderLayout()
        val tagPane = JPanel(FlowLayout())
        val hedTagInput = JTextArea(3, 10)
        hedTagInput.preferredSize = Dimension(300,300)
        val inputPane = JPanel()
        inputPane.preferredSize = Dimension(300, 300)
        inputPane.add(hedTagInput)
        val list: JList<String>
        val listModel = DefaultListModel<String>()
        list = JList(listModel)
        hedTagInput.document.addDocumentListener(HedTagInputListener(hedTagInput, list, listModel))
        val searchPanel = JScrollPane(list)
        searchPanel.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        searchPanel.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        searchPanel.preferredSize = Dimension(300,200)
        tagPane.add(inputPane)
        tagPane.add(searchPanel)
        mainPane.add(tagPane, BorderLayout.CENTER)
        mainPane.add(JButton("Done"), BorderLayout.SOUTH)

        frame.background = BLUE_MEDIUM
        mainPane.background = BLUE_MEDIUM
        tagPane.background = BLUE_MEDIUM
        inputPane.background = BLUE_MEDIUM
        frame.pack()
        frame.isVisible = true
    }

    class HedTagInputListener(val tc: JTextArea, val list: JList<String>, val listModel: DefaultListModel<String>) : DocumentListener {
        init{
            list.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(evt: MouseEvent) {
                    val list = evt.getSource() as JList<String>
                    if (evt.getClickCount() === 2) {

                        // Double-click detected
                        val index = list.locationToIndex(evt.getPoint())
                        val selectedTag = list.selectedValue
                        replaceWithTag(selectedTag)
                    } else if (evt.getClickCount() === 3) {

                        // Triple-click detected
                        val index = list.locationToIndex(evt.getPoint())
                    }
                }
            })
        }
        private fun replaceWithTag(selectedTag: String) {
            try {
                var wordStartPos = findBeginning()
                val lastNode = selectedTag.split('/').last()
                tc.replaceRange(null, wordStartPos, tc.caretPosition)
                tc.insert(lastNode, wordStartPos)
            } catch (e: BadLocationException) {
                System.err.println(e)
            }
        }
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
            listModel.clear()
            matchedTags.forEach { listModel.addElement(it) }
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
