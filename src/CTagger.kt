import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.io.IOException
import java.io.StringReader
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.JTextComponent
import javax.swing.text.Utilities
import javax.xml.bind.JAXBContext


var tags: MutableList<String> = mutableListOf()
var hedXmlModel = HedXmlModel()

class App {
    val frame: JFrame
    val hedTagInput: JTextArea

    constructor() {
        frame = JFrame("CTAGGER IDE version")
        frame.size = Dimension(800, 800)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        frame.setLocation(dim.width / 2 - frame.size.width / 2, dim.height / 2 - frame.size.height / 2)

        val mainPane = JPanel(BorderLayout())
        hedTagInput = JTextArea(30, 30)
        hedTagInput.document.addDocumentListener(HedTagInputListener())
        hedTagInput.document.putProperty("owner", hedTagInput) //set the owner

        mainPane.add(hedTagInput, BorderLayout.CENTER)

        frame.contentPane.add(mainPane)

    }

    fun startApp() {
        frame.pack()
        frame.repaint()
        frame.isVisible = true
    }



    class HedTagInputListener : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) {
            val tc = e.document.getProperty("owner")
            if (tc is JTextArea) {
                val word = getWordAtCaret(tc, "insert")
                if (word != null) searchTags(word).map{println(it)}
            }
        }
        override fun changedUpdate(e: DocumentEvent) {

        }

        override fun removeUpdate(e: DocumentEvent) {
            val tc = e.document.getProperty("owner")
            if (tc is JTextArea) {
                val word = getWordAtCaret(tc)
                if (word != null) searchTags(word).map{println(it)}
            }
        }
        fun getWordAtCaret(tc: JTextComponent, type: String = ""): String? {
            try {
                var caretPosition = tc.caretPosition
                if (type == "insert") ++caretPosition
                var wordStartPos = caretPosition - 1
                while (wordStartPos > 0 && tc.getText(wordStartPos-1, 1) != " ") {
                    --wordStartPos
                }
                    val word = tc.getText(wordStartPos, caretPosition - wordStartPos)
                    println(word)
                    return word
            } catch (e: BadLocationException) {
                System.err.println(e)
            }
            return null
        }
        protected fun searchTags(input: String): List<String> {
            // TODO optimize
            return tags.filter{ it.contains(input, true)} // beautiful syntax comparing to Java!
        }
    }
}

fun main() {
    getHedXmlModel()
    val app = App()
    app.startApp()
}

fun getHedXmlModel() {
    val xmlData = TestUtilities.getResourceAsString(TestUtilities.HedFileName)
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
