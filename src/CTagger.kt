import com.google.gson.Gson
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowEvent
import java.io.StringReader
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.xml.bind.JAXBContext



fun main() {
    SwingUtilities.invokeLater { CTagger() }
}

class CTagger {
    val frame: JFrame
    val tags: MutableList<String> = mutableListOf()
    val finalMap = HashMap<String, String>()
    var eventCodeList: EventCodeList
    var hedTagInput: HedTagInput
    var hedTagList: HedTagList
    val BLUE_MEDIUM = Color(168, 194, 255)

    init {
        getHedXmlModel()
        eventCodeList = EventCodeList(setOf("1", "2", "3"), this)
        hedTagInput = HedTagInput(this)
        hedTagList = HedTagList(tags)
        hedTagList.hedInput = hedTagInput
        hedTagInput.tagList = hedTagList
    }
    constructor() {
        frame = JFrame("CTAGGER IDE version")
        frame.setSize(1024, 800)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        frame.setLocation(dim.width / 2 - frame.size.width / 2, dim.height / 2 - frame.size.height / 2)

        val mainPane = frame.contentPane
        mainPane.layout = BorderLayout()

        addCodePane(mainPane)
        addTagPane(mainPane)
        addDoneBtn(mainPane)

        frame.background = BLUE_MEDIUM
        mainPane.background = BLUE_MEDIUM
        frame.pack()
        frame.isVisible = true
    }
    private fun addCodePane(mainPane: Container) {
        val codePane = JPanel(FlowLayout())
        codePane.add(JLabel("Code: "))
        val codeTF = JTextField(20)
        codePane.add(codeTF)
        codePane.background = BLUE_MEDIUM

        mainPane.add(codePane, BorderLayout.NORTH)
    }
    private fun addTagPane(mainPane: Container) {
        val eventPanel = JScrollPane(eventCodeList)
        eventPanel.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        eventPanel.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        eventPanel.preferredSize = Dimension(300,300)

        val searchPanel = JScrollPane(hedTagList)
        searchPanel.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        searchPanel.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        searchPanel.preferredSize = Dimension(300,300)

        hedTagInput.preferredSize = Dimension(300,300)
        val inputPane = JScrollPane(hedTagInput)
        inputPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER // Force wrapping. Deduced from: http://java-sl.com/wrap.html
        inputPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

        val tagPane = JPanel(BorderLayout())
        tagPane.add(eventPanel, BorderLayout.LINE_START)
        tagPane.add(inputPane, BorderLayout.CENTER)
        tagPane.add(searchPanel, BorderLayout.LINE_END)

        eventPanel.background = BLUE_MEDIUM
        inputPane.background = BLUE_MEDIUM
        tagPane.background = BLUE_MEDIUM

        mainPane.add(tagPane, BorderLayout.CENTER)
    }

    private fun addDoneBtn(mainPane: Container) {
        val doneBtn = JButton("Done")
        doneBtn.addActionListener {
            val gson = Gson()
            val finalJson = gson.toJson(finalMap).toString()

            println(finalJson)
            JOptionPane.showMessageDialog(frame,
                    finalJson,
                    "HED tag final JSON string",
                    JOptionPane.PLAIN_MESSAGE)
            frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
        }

        mainPane.add(doneBtn, BorderLayout.SOUTH)
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
}

