import com.google.gson.Gson
import com.univocity.parsers.tsv.TsvParser
import com.univocity.parsers.tsv.TsvParserSettings
import java.awt.*
import java.awt.event.WindowEvent
import java.io.File
import java.io.StringReader
import javax.swing.*
import javax.xml.bind.JAXBContext



fun main() {
    SwingUtilities.invokeLater { CTagger() }
}

class CTagger {
    val frame = JFrame("CTAGGER")
    private val tags: MutableList<String> = mutableListOf()
    val fieldMap = HashMap<String, HashMap<String,String>>()
    var fieldCB = JComboBox<String>()
    var eventCodeList: EventCodeList
    var hedTagInput: HedTagInput
    var hedTagList: HedTagList
    var curField: String? = null
    lateinit var eventFile: Array<Array<String>>
    lateinit var eventFileAnnotation: EventFileAnnotation
    var fieldAndUniqueCodeMap = HashMap<String, List<String>>()
    private val BLUE_MEDIUM = Color(168, 194, 255)

    init {
        getHedXmlModel()
        eventCodeList = EventCodeList(this)
        hedTagInput = HedTagInput(this)
        hedTagList = HedTagList(tags)
        hedTagList.hedInput = hedTagInput
        hedTagInput.tagList = hedTagList
    }
    constructor() {
        frame.setSize(1024, 800)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        frame.setLocation(dim.width / 2 - frame.size.width / 2, dim.height / 2 - frame.size.height / 2)

        val mainPane = frame.contentPane
        mainPane.layout = BorderLayout()

        createMenuBar()
        addFieldSelectionPane(mainPane)
        addTagPane(mainPane)
        addDoneBtn(mainPane)

        frame.background = BLUE_MEDIUM
        mainPane.background = BLUE_MEDIUM
        frame.pack()
        frame.isVisible = true
    }

    private fun createMenuBar() {
        val menuBar = JMenuBar()

        // File menu item
        var menu = JMenu("File")
        menuBar.add(menu)

        // Import event file menu item
        var submenu = JMenu("Import")
        var menuItem = JMenuItem("Import BIDS events.tsv file")
        menuItem.addActionListener {
            val fc = JFileChooser()
            val fileChosen = fc.showOpenDialog(frame)
            if (fileChosen == JFileChooser.APPROVE_OPTION) {
                val file = fc.selectedFile
                getEventInfo(file)
            }
        }
        submenu.add(menuItem)
        menuItem = JMenuItem("Import tag file")
        submenu.add(menuItem)
        menu.add(submenu)

        frame.setJMenuBar(menuBar)
    }

    // Retrieve event info from .tsv file
    // and populate GUI
    private fun getEventInfo(file: File) {
        // reset if not empty
        if (fieldMap.isNotEmpty()) {
            JOptionPane.showMessageDialog(frame, "Clearing fieldMap")
            curField = null
            fieldAndUniqueCodeMap.clear()
            fieldMap.clear()
            fieldCB.removeAll()
        }
        val settings = TsvParserSettings()
        val parser = TsvParser(settings)
        val allRows = parser.parseAll(file)
        eventFile = allRows.toTypedArray()
        val eventFileColumnMajor = Array(allRows[0].size) { Array(allRows.size) {""} }
        for ((rowIndex, row) in allRows.withIndex()) {
            for ((colIndex, col) in row.withIndex()) {
                eventFileColumnMajor[colIndex][rowIndex] = col
            }
        }
        eventFileColumnMajor.forEach {
            // assuming that first row contains field names as BIDS TSV
            val field = it[0]
            // add fields to combo box
            fieldCB.addItem(field)
            // add unique codes to each field, ignoring BIDS default numerical fields
            if (!listOf("duration", "onset", "sample", "stim_file", "HED", "response_time").contains(field)) {
                fieldAndUniqueCodeMap[field] = it.slice(1 until it.size).distinct()
            } else {
                fieldAndUniqueCodeMap[field] = listOf("hed_string")
            }
            // initialize fieldMap
            fieldMap[field] = HashMap()
            fieldAndUniqueCodeMap[field]!!.forEach { fieldMap[field]!![it] = "" }

            // initialize tagging GUI
            eventFileAnnotation = EventFileAnnotation(frame, this)
        }
    }
    private fun addFieldSelectionPane(mainPane: Container) {
        val fieldSelectionPane = JPanel(FlowLayout())
        fieldSelectionPane.add(JLabel("Tagging field: "))
        fieldCB.addActionListener {
            // save work of previous field
            if (curField != null) {
                val map = fieldMap[curField!!]
                val key = eventCodeList.prevSelected
                eventCodeList.prevSelected = null
                if (map != null && key != null)
                    map[key] = hedTagInput.text
                hedTagInput.text = null
            }
            // set new field and new code list
            curField = fieldCB.selectedItem.toString()
            if (curField != null && fieldAndUniqueCodeMap.containsKey(curField!!)) {
                // get unique event codes
                eventCodeList.codeSet = fieldAndUniqueCodeMap[curField!!]!!
            }
        }
        fieldSelectionPane.add(fieldCB)
        val eventFileTagBtn = JButton("Tag event file")
        eventFileTagBtn.addActionListener {
            eventFileAnnotation.isVisible = true
        }
        fieldSelectionPane.add(eventFileTagBtn)
        fieldSelectionPane.background = BLUE_MEDIUM

        mainPane.add(fieldSelectionPane, BorderLayout.NORTH)
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
            val finalJson = gson.toJson(fieldMap).toString()

            println(finalJson)
            JOptionPane.showMessageDialog(frame,
                    finalJson,
                    "HED tag final JSON string",
                    JOptionPane.PLAIN_MESSAGE)
            frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
        }

        mainPane.add(doneBtn, BorderLayout.SOUTH)
    }

    // Parse HED XML
    private fun getHedXmlModel() {
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

    // Add tags recursively
    private fun populateTagSets(path: String, tagSets: Set<TagXmlModel>) {
        for (tagXmlModel: TagXmlModel in tagSets) {
            tags.add(path + tagXmlModel.name)
            populateTagSets(path + tagXmlModel.name + "/", tagXmlModel.tags)
        }
    }
}

