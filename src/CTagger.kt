import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.univocity.parsers.tsv.TsvParser
import com.univocity.parsers.tsv.TsvParserSettings
import tornadofx.launch
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.StringReader
import javax.swing.*
import javax.xml.bind.JAXBContext


fun main() {
    SwingUtilities.invokeLater { CTagger() }
}

class CTagger {
    val frame = JFrame("CTAGGER")
    var hedVersion = ""
    val tags: MutableList<String> = mutableListOf()
    val fieldMap = HashMap<String, HashMap<String,String>>()
    var fieldCB = JComboBox<String>()
    var eventCodeList: EventCodeList
    lateinit var hedTagInput: HedTagInput
    lateinit var hedTagList: HedTagList
    lateinit var searchResultPanel: JScrollPane
    var schemaView: SchemaView
    val inputPane = JLayeredPane()
    lateinit var eventFile: Array<Array<String>>
    lateinit var eventFileAnnotation: EventFileAnnotation
    var fieldAndUniqueCodeMap = HashMap<String, List<String>>()
    private val BLUE_MEDIUM = Color(168, 194, 255)
    private val isValueField = HashMap<String, Boolean>()
    private val oldFieldAndUniqueCodeMap = HashMap<String, List<String>>()
    private var javaFxLaunched = false



    init {
        getHedXmlModel()
        eventCodeList = EventCodeList(this)
        getEventInfo(File("/Users/dtyoung/test_events.tsv"))
    }
    constructor() {
        frame.setSize(1324, 800)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        frame.setLocation(dim.width / 2 - frame.size.width / 2, dim.height / 2 - frame.size.height / 2)

        schemaView = SchemaView(this)

        val mainPane = frame.contentPane
        mainPane.layout = BorderLayout()

        createMenuBar()
        addFieldSelectionPane(mainPane)
        addCenterPane(mainPane)
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

        menuItem = JMenuItem("Show BIDS format")
        menuItem.addActionListener {
            showBIDSWindow()
        }
        menu.add(menuItem)

        // View menu item
        menu = JMenu("View")
        menuBar.add(menu)
        menuItem = JMenuItem("Show HED Schema")
        menuItem.addActionListener {
            schemaView.show()
        }
        menu.add(menuItem)

        frame.setJMenuBar(menuBar)
    }

    // Retrieve event info from .tsv file
    // and populate GUI
    private fun getEventInfo(file: File) {
        // reset if not empty
        if (fieldMap.isNotEmpty()) {
            JOptionPane.showMessageDialog(frame, "Clearing fieldMap")
//            curField = null
            fieldAndUniqueCodeMap.clear()
            fieldMap.clear()
            isValueField.clear()
            oldFieldAndUniqueCodeMap.clear()
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
                isValueField[field] = false
            } else {
                fieldAndUniqueCodeMap[field] = listOf("HED")
                isValueField[field] = true
            }
            // initialize fieldMap
            fieldMap[field] = HashMap()
            fieldAndUniqueCodeMap[field]!!.forEach { fieldMap[field]!![it] = "" }
        }
        // initialize tagging GUI
        eventCodeList.codeSet = fieldAndUniqueCodeMap[fieldCB.selectedItem!!]!! // add codes of current field
        eventCodeList.selectedIndex = 0 // select first code in the list
        eventFileAnnotation = EventFileAnnotation(frame, this)
    }
    private fun addFieldSelectionPane(mainPane: Container) {
        val fieldSelectionPane = JPanel(FlowLayout())
        fieldSelectionPane.add(JLabel("Tagging field: "))
        fieldCB.addItemListener {
            if (it.stateChange == ItemEvent.DESELECTED) {
                val curField = it.item as String
                println(curField)
                // save current work
                if (curField != null) {
                    val map = fieldMap[curField!!]
                    val key = eventCodeList.selectedValue
//                    eventCodeList.prevSelected = null
                    println(fieldMap)
                    println(key)
                    println(map)
                    if (map != null && key != null)
                        map[key] = hedTagInput.getCleanHEDString()
                    println(map)
                    println(fieldMap)
                    hedTagInput.text = null
                }
                // set new field and new code list
                if (fieldCB.selectedItem != null && fieldAndUniqueCodeMap.containsKey(fieldCB.selectedItem.toString()!!)) {
                    // get unique event codes
                    eventCodeList.codeSet = fieldAndUniqueCodeMap[fieldCB.selectedItem!!]!!
                }
            }
        }
        fieldSelectionPane.add(fieldCB)
        val addFieldBtn = JButton("Create new field")
        addFieldBtn.addActionListener {

//            public static void myLaunch(Class<? extends Application> applicationClass) {
//                if (!javaFxLaunched) { // First time
//                    Platform.setImplicitExit(false);
//                    new Thread(()->Application.launch(applicationClass)).start();
//                    javaFxLaunched = true;
//                } else { // Next times
//                    Platform.runLater(()->{
//                        try {
//                            Application application = applicationClass.newInstance();
//                            Stage primaryStage = new Stage();
//                            application.start(primaryStage);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    });
//                }
//            }
            val gson = Gson()
            val finalJson = gson.toJson(fieldAndUniqueCodeMap).toString()
            launch<FieldGenerator>("{\"newFieldName\": \"trial_type\", \"eventFields\": $finalJson}")
        }
        fieldSelectionPane.add(addFieldBtn)

        val eventFileTagBtn = JButton("Tag event file")
        eventFileTagBtn.addActionListener {
            eventFileAnnotation.isVisible = true
        }
//        fieldSelectionPane.add(eventFileTagBtn)
        fieldSelectionPane.background = BLUE_MEDIUM

        mainPane.add(fieldSelectionPane, BorderLayout.NORTH)
    }
    private fun addCenterPane(mainPane: Container) {
        val eventPane = JPanel()
        eventPane.layout = BoxLayout(eventPane, BoxLayout.PAGE_AXIS)
        val isValueCheckbox = JCheckBox("Check if value field")
        isValueCheckbox.addItemListener {
            if (it.stateChange == 1)
                setValueField(fieldCB.selectedItem.toString())
            else
                unsetValueField(fieldCB.selectedItem.toString())
        }
        eventPane.add(isValueCheckbox)

        val eventCodePane = JScrollPane(eventCodeList)
        eventCodePane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        eventCodePane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        eventCodePane.preferredSize = Dimension(300,300)
        eventCodePane.background = BLUE_MEDIUM
        eventPane.add(eventCodePane)

        inputPane.preferredSize = Dimension(600,300)
        hedTagInput = HedTagInput(this)
        hedTagInput.setBounds(0,0, 600,300)
        val tagInputPane = JScrollPane(hedTagInput)
        tagInputPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER // Force wrapping. Deduced from: http://java-sl.com/wrap.html
        tagInputPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        tagInputPane.setBounds(0,0,600,300)
        tagInputPane.location = Point(0,0)

        hedTagList = HedTagList(this, tags, hedTagInput)
        searchResultPanel = JScrollPane(hedTagList)
        searchResultPanel.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        searchResultPanel.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        searchResultPanel.setBounds(0, 0, 580,150)
        searchResultPanel.location = Point(15,150)
        searchResultPanel.isVisible = false

        inputPane.add(tagInputPane)

        val tagPane = JPanel(GridLayout(1,2))
        tagPane.add(eventPane)
        tagPane.add(inputPane)
        tagPane.background = BLUE_MEDIUM

        mainPane.add(tagPane, BorderLayout.CENTER)
    }

    private fun addDoneBtn(mainPane: Container) {
        val doneBtn = JButton("Done")
        doneBtn.addActionListener {
            val finalJson = prettyPrintJson(exportBIDSJson(fieldMap))
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
            hedVersion = hedXmlModel.version
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

    fun hideSearchResultPane() {
        SwingUtilities.invokeLater {
            searchResultPanel.isVisible = false
            inputPane.remove(searchResultPanel)
            inputPane.repaint()
        }
    }

    fun showSearchResultPane(x: Int, y: Int) {
        SwingUtilities.invokeLater {
            searchResultPanel.location = Point(x, y)
            searchResultPanel.revalidate()
            searchResultPanel.repaint()
            searchResultPanel.isVisible = true
            inputPane.add(searchResultPanel)
            inputPane.setLayer(searchResultPanel, 1)
            inputPane.repaint()
        }
    }

    fun setValueField(field: String) {
        isValueField[field] = true
        if (field in fieldAndUniqueCodeMap) {
            oldFieldAndUniqueCodeMap[field] = fieldAndUniqueCodeMap[field]!!
            fieldAndUniqueCodeMap[field] = listOf("HED")
            eventCodeList.codeSet = fieldAndUniqueCodeMap[field]!!
        }
        if (field in fieldMap) {
            fieldMap[field]!!.clear()
            fieldMap[field]!!["HED"] = ""
        }
    }
    fun unsetValueField(field: String) {
        if (field in isValueField && isValueField[field]!!) {
            isValueField[field] = false
            if (field in oldFieldAndUniqueCodeMap) {
                fieldAndUniqueCodeMap[field] = oldFieldAndUniqueCodeMap[field]!!
                eventCodeList.codeSet = fieldAndUniqueCodeMap[field]!!
                if (field in fieldMap) {
                    fieldMap[field]!!.clear()
                    fieldAndUniqueCodeMap[field]!!.forEach{fieldMap[field]!![it] = ""}
                }
            }
        }
    }

    /**
     * Export field map (fMap) to json accordingly to HED-BIDS specification
     * https://bids-specification.readthedocs.io/en/stable/99-appendices/03-hed.html#appendix-iii-hierarchical-event-descriptors
     */
    fun exportBIDSJson(fMap: HashMap<String, HashMap<String,String>>): HashMap<String, Any> {
        val bidsFieldMap = HashMap<String, Any>()
//        val gson = Gson()
        fMap.forEach {
            // value type, ignoring empty fields
            if (isValueField.containsKey(it.key) && isValueField[it.key]!! && it.value.containsKey("HED") && it.value["HED"]!!.isNotEmpty())
                bidsFieldMap[it.key] = hashMapOf(Pair("HED",it.value["HED"]!!))
            else { // categories
                val finalMap = HashMap<String,String>()
                it.value.forEach {map ->
                    // ignore empty codes
                    if (map.value.isNotEmpty())
                        finalMap[map.key] = map.value
                }
                if (finalMap.isNotEmpty())
                    bidsFieldMap[it.key] = hashMapOf(Pair("HED", finalMap))
            }
        }
//        return gson.toJson(bidsFieldMap)
        return bidsFieldMap
    }
    fun prettyPrintJson(fieldMap: HashMap<String, Any>): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(fieldMap)
    }

    fun showBIDSWindow() {
        val json = prettyPrintJson(exportBIDSJson(fieldMap))
        val textarea = JTextArea(10,20)
        textarea.text = json
        textarea.isEditable = false
        val scroller = JScrollPane(textarea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        val options1 = arrayOf<Any>("Save to file", "Ok")
        val result = JOptionPane.showOptionDialog(frame, scroller, "BIDS events.json HED string", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, 1)
        // if save to file selected
        if (result == 0) {
            val fileChooser = JFileChooser()
            fileChooser.selectedFile = File("_events.json")
            val retval = fileChooser.showSaveDialog(frame)
            if (retval == JFileChooser.APPROVE_OPTION) {
                var file = fileChooser.selectedFile ?: return
                if (!file.name.toLowerCase().endsWith(".json")) {
                    file = File(file.parentFile, file.name + ".json")
                }
                try {
                    textarea.write(OutputStreamWriter(FileOutputStream(file),
                            "utf-8"))
//                    Desktop.getDesktop().open(file)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        print(result)
    }
}

