import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.univocity.parsers.tsv.TsvParser
import com.univocity.parsers.tsv.TsvParserSettings
import java.awt.*
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.StringReader
import java.lang.reflect.Type
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.xml.bind.JAXBContext
import kotlin.concurrent.timer


fun main() {
    SwingUtilities.invokeLater { CTagger(isJson = false, isTSV = false, filename = "", isScratch=true) }
}

class CTagger(val isJson: Boolean, var isTSV: Boolean, var filename:String, var isScratch:Boolean) {
    var isVerbose = false
    private val frame = JFrame("CTAGGER")
    var hedVersion = ""
    lateinit var unitClasses: Set<UnitClassXmlModel>
    lateinit var unitModifiers: ArrayList<UnitModifierXmlModel>
    val tags: MutableList<String> = mutableListOf()
    private val schema: HashMap<String, TagModel> = HashMap()
    lateinit var hedValidator: HedValidator
    val fieldList = FieldList(this)
    var eventCodeList: EventCodeList
    lateinit var hedTagInput: HedTagInput
    lateinit var hedTagList: HedTagList
    lateinit var searchResultPanel: JScrollPane
    private var schemaView: SchemaView
    private val inputPane = JLayeredPane()

    init {
        getHedXmlModel()
        eventCodeList = EventCodeList(this)

        if (isTSV)
            importBIDSEventTSV(File(filename))
        else if (isJson)
            importBIDSEventJson(File(filename))
        else {
            isScratch = true
            importBIDSEventJson(File(TestUtilities.ScratchJsonFileName))
        }

        frame.setSize(1000, 800)
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

        // set default background color to all panels and dialogs
        UIManager.put("Panel.background", Style.BLUE_MEDIUM)
        UIManager.put("OptionPane.background", Style.BLUE_MEDIUM)
        SwingUtilities.updateComponentTreeUI(frame)
        frame.pack()
        frame.isResizable = false
        frame.isVisible = true

        // start saving thread
        timer(name = "Save tags", period = 3000) {
            if (isVerbose) println("Saving tags")
            // save current tags
            try {
                val curField = fieldList.selectedItem.toString()
                val fieldMap = fieldList.fieldMap
                if (curField != null && fieldMap.containsKey(curField)) {
                    val codeMap = fieldMap[curField]
                    val selected = eventCodeList.selectedValue
                    if (selected != null && codeMap!!.containsKey(selected))
                        codeMap!![selected] = hedTagInput.getCleanHEDString()
                }
            }
            catch (e: Exception) {
                // Simply ignore and not save
            }
        }
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
            JOptionPane.showMessageDialog(frame,
                    "The first row should contain event field names", "Warning", JOptionPane.WARNING_MESSAGE)
            val fc = JFileChooser()
            val fileChosen = fc.showOpenDialog(frame)
            if (fileChosen == JFileChooser.APPROVE_OPTION) {
                val file = fc.selectedFile
                importBIDSEventTSV(file)
            }
        }
        submenu.add(menuItem)
        menuItem = JMenuItem("Import BIDS events.json file")
        menuItem.addActionListener {
            val fc = JFileChooser()
            val fileChosen = fc.showOpenDialog(frame)
            if (fileChosen == JFileChooser.APPROVE_OPTION) {
                val file = fc.selectedFile
                importBIDSEventJson(file)
            }
        }
        submenu.add(menuItem)
        menuItem = JMenuItem("Import tag file")
        submenu.add(menuItem)
        menu.add(submenu)

        menuItem = JMenuItem("Review all tags")
        menuItem.addActionListener {
            showJsonWindow()
        }
        menu.add(menuItem)

        // View menu item
//        menu = JMenu("View")
//        menuBar.add(menu)
//        menuItem = JMenuItem("Show HED Schema")
//        menuItem.addActionListener {
//            schemaView.show()
//        }
//        menu.add(menuItem)

        frame.setJMenuBar(menuBar)
    }

    private fun addFieldSelectionPane(mainPane: Container) {
        val fieldSelectionPane = JPanel(FlowLayout())
        val fieldSelectionPaneLabel = JLabel("Tagging field: ")
        fieldSelectionPane.add(fieldSelectionPaneLabel)
        fieldSelectionPaneLabel.foreground = Style.BLUE_DARK
        fieldSelectionPaneLabel.font = Font("Sans Serif", Font.BOLD, 12)
        fieldList.initializeListener()
        fieldSelectionPane.add(fieldList)
        mainPane.add(fieldSelectionPane, BorderLayout.NORTH)
    }

    /**
     * Add Event code and tag Input panes
     */
    private fun addCenterPane(mainPane: Container) {
        val centerPane = JPanel(GridBagLayout())
        var c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.gridwidth = 2
        c.anchor = GridBagConstraints.LINE_START
        val eventPaneLabel = JLabel("Event code")
        eventPaneLabel.border = EmptyBorder(0,10,0,0)
        eventPaneLabel.foreground = Style.BLUE_DARK
        eventPaneLabel.font = Font("Sans Serif", Font.BOLD, 12)
        centerPane.add(eventPaneLabel, c)

        c = GridBagConstraints()
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 0
        c.gridy = 1
        c.gridwidth = 2
        c.weightx = 0.5
        c.insets = Insets(0,10,0,0)
        val eventCodePane = JScrollPane(eventCodeList)
        eventCodePane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        eventCodePane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        eventCodePane.preferredSize = Dimension(500,300)
        eventCodePane.background = Style.BLUE_MEDIUM
        centerPane.add(eventCodePane, c)

        c = GridBagConstraints()
        c.gridx = 2
        c.gridy = 0
        c.gridwidth = 1
        c.anchor = GridBagConstraints.LINE_START
        val tagPanelLabel = JLabel("HED tags")
        tagPanelLabel.font = Font("Sans Serif", Font.BOLD, 12)
        tagPanelLabel.border = EmptyBorder(0,10,0,0)
        tagPanelLabel.foreground = Style.BLUE_DARK
        centerPane.add(tagPanelLabel)

        c = GridBagConstraints()
        c.gridx = 3
        c.gridy = 0
        c.gridwidth = 3
        c.anchor = GridBagConstraints.LINE_END
        val showSchemaBtn = JButton("Show HED schema")
        showSchemaBtn.addActionListener {
            schemaView.show()
        }
        centerPane.add(showSchemaBtn, c)

        c = GridBagConstraints()
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 2
        c.gridy = 1
        c.gridwidth = 4
        c.weightx = 1.0
        c.insets = Insets(0,0,0,5)
        inputPane.layout = FlowLayout()
        hedTagInput = HedTagInput(this)
        hedTagInput.preferredSize = Dimension(500,300)
        val tagInputPaneScrollPane = JScrollPane(hedTagInput)
        tagInputPaneScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER // Force wrapping. Deduced from: http://java-sl.com/wrap.html
        tagInputPaneScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        tagInputPaneScrollPane.location = Point(0,0)
        inputPane.add(tagInputPaneScrollPane, 0)
        centerPane.add(inputPane, c)

        hedTagList = HedTagList(this, tags, hedTagInput)
        searchResultPanel = JScrollPane(hedTagList)
        searchResultPanel.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        searchResultPanel.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        searchResultPanel.setBounds(0, 0, 480,150)
        searchResultPanel.location = Point(30,150)
        searchResultPanel.isVisible = false

        mainPane.add(centerPane, BorderLayout.CENTER)
    }

    private fun addDoneBtn(mainPane: Container) {
        val doneBtn = JButton("Done")
        doneBtn.addActionListener {
            showJsonWindow()
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
        unitClasses = hedXmlModel.unitClasses.unitClasses
        unitModifiers = hedXmlModel.unitModifiers.unitModifiers
        populateTagSets("", hedXmlModel.tags, false)
        hedValidator = HedValidator(schema, this)
    }

    // Add tags recursively
    private fun populateTagSets(path: String, tagSets: Set<TagXmlModel>, parentExtensionAllowed: Boolean) {
        for (tagXmlModel: TagXmlModel in tagSets) {
            if (parentExtensionAllowed)
                tagXmlModel.isExtensionAllowed = parentExtensionAllowed
            val tagPath = path + tagXmlModel.name
            val tagModel = TagModel(tagPath, tagXmlModel)
            val nodes = tagPath.split('/')
            for (i in nodes.size-1 downTo 0) {
                val path = nodes.subList(i, nodes.size).joinToString("/")
                if (path != "#")
                    schema[path] = tagModel
            }
            tags.add(tagPath)
            populateTagSets("$tagPath/", tagXmlModel.tags, tagXmlModel.isExtensionAllowed)
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
            inputPane.add(searchResultPanel, Integer(1),0)
            inputPane.repaint()
        }
    }

    /**
     * Export field map (fMap) to json accordingly to HED-BIDS specification
     * https://bids-specification.readthedocs.io/en/stable/99-appendices/03-hed.html#appendix-iii-hierarchical-event-descriptors
     */
    fun exportToJson(fMap: HashMap<String, HashMap<String,String>>): HashMap<String, Any> {
        val jsonFieldMap = HashMap<String, Any>()
        fMap.forEach {
            // value type, ignoring empty fields
            if (fieldList.isValueField.containsKey(it.key) && fieldList.isValueField[it.key]!! && it.value.containsKey("HED") && it.value["HED"]!!.isNotEmpty())
                jsonFieldMap[it.key] = hashMapOf(Pair("HED", it.value["HED"]!!))
            // categorical fields
            else {
                val finalMap = HashMap<String, String>()
                it.value.forEach { map ->
                    // ignore empty codes
                    if (map.value.isNotEmpty()) {
                        // some clean-up
                        var finalString = map.value
                        finalString = finalString.replace("\n", "")
                        finalMap[map.key] = finalString
                    }
                }
                if (finalMap.isNotEmpty())
                    jsonFieldMap[it.key] = hashMapOf(Pair("HED", finalMap))
            }
        }
        return jsonFieldMap
    }
    fun prettyPrintJson(fieldMap: HashMap<String, Any>): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(fieldMap)
    }

    private fun showJsonWindow() {
        val json = prettyPrintJson(exportToJson(fieldList.fieldMap))
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
    }

    /**
     * Retrieve event info from .tsv file
     * and populate GUI
     */
    private fun importBIDSEventTSV(file: File) {
        try {
            val settings = TsvParserSettings()
            settings.format.lineSeparator = "\n".toCharArray()
            val parser = TsvParser(settings)
            // parse file as a collection of rows
            val allRows = parser.parseAll(file)

            // show dialog to verify categorical fields
            var categoricalField = mutableListOf<String>()
            var isCancelled = false
            // create dialog
            val dialog = JDialog(frame, "", true)
            val pane = JPanel(BorderLayout())
            pane.border = EmptyBorder(10, 10, 10, 10)
            val label = JLabel("Select categorical event fields (Hold Ctrl/Cmd for multiple selections)")
            label.border = EmptyBorder(10,0,10,0)
            pane.add(label, BorderLayout.PAGE_START)
            val list = JList<String>(allRows[0])
            pane.add(list, BorderLayout.CENTER)
            val btnPane = JPanel()
            val okBtn = JButton("Ok")
            okBtn.addActionListener {
                categoricalField = list.selectedValuesList
                if (categoricalField.isEmpty())
                    JOptionPane.showMessageDialog(frame, "No field selected. Treating all fields as numerical fields.")
                dialog.dispose()
            }
            val cancelBtn = JButton("Cancel")
            cancelBtn.addActionListener{
                dialog.dispose()
                isCancelled = true
            }
            btnPane.add(okBtn)
            btnPane.add(cancelBtn)
            pane.add(btnPane, BorderLayout.PAGE_END)
            dialog.contentPane = pane
            dialog.pack()
            dialog.isVisible = true

            // user selected categorical fields. Proceed
            if (!isCancelled) {
                val eventFile = Array(allRows[0].size) { Array(allRows.size) { "" } }
                for ((rowIndex, row) in allRows.withIndex()) {
                    for ((colIndex, col) in row.withIndex()) {
                        eventFile[colIndex][rowIndex] = col
                    }
                }

                // parse successfully. Creating fields
                // reset if not empty
                if (!fieldList.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Clearing old fields")
                    fieldList.clear()
                }
                eventFile.forEach { column ->
                    // add fields to combo box
                    fieldList.addFieldFromColumn(column, categoricalField.contains(column[0]))
                }
                // initialize tagging GUI
                eventCodeList.codeSet = fieldList.fieldAndUniqueCodeMap[fieldList.selectedItem!!]!! // add codes of current field
                eventCodeList.selectedIndex = 0 // select first code in the list
                fieldList.repaint()
            }
        }
        catch (e: Exception) {
            JOptionPane.showMessageDialog(frame, "Error importing BIDS _events.tsv", "Import error", JOptionPane.ERROR_MESSAGE)
        }
    }
    /**
     * Action listener for import events.json menu item
     * Importing events.json and update GUI
     */
    private fun importBIDSEventJson(file: File) {
        try {
            val json: String = file.readText()
            val type: Type = object : TypeToken<HashMap<String?, BIDSFieldDict>?>() {}.type
            val gson = Gson()
            val result: HashMap<String, BIDSFieldDict> = gson.fromJson(json, type)
            println(result)
            // reset if not empty
            if (!fieldList.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Clearing old fields")
                fieldList.clear()
            }
            result.forEach {
                // assuming that first row contains field names as BIDS TSV
                // add fields to combo box
                fieldList.addFieldFromDict(it.key.toLowerCase(), it.value)
            }
            // initialize/update tagging GUI
            eventCodeList.codeSet = fieldList.fieldAndUniqueCodeMap[fieldList.selectedItem!!]!! // add codes of current field
            eventCodeList.selectedIndex = 0 // select first code in the list
            fieldList.repaint()
        }
        catch (e: Exception) {
            JOptionPane.showMessageDialog(frame, "Error importing BIDS _events.json", "Import error", JOptionPane.ERROR_MESSAGE)
        }
    }

    /**
     * For deserialization of events.json
     */
    class BIDSFieldDict{
        var LongName:String = ""
        var Description:String = ""
        var Levels:HashMap<String,String> = HashMap()
        var Units:String = ""
        var HED:Any = ""
    }


}

