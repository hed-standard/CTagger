import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class EventCodeList(val tagger: CTagger) : JList<String>() {
    private val listModel = DefaultListModel<String>()
    var prevSelected: String? = null
    var codeSet: List<String> = listOf()
        set(value) {
            field = value
            listModel.clear()
            updateListModel()
        }

    init {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        model = listModel
        addMouseListener(EventCodeListMouseListener(this))
        addKeyListener(EventCodeListKeyListener(this))
    }

    private fun updateListModel() {
        if (listModel.isEmpty)
            codeSet.forEach{ listModel.addElement(it) }
    }
    private class EventCodeListKeyListener(private val parent:EventCodeList): KeyListener {
        override fun keyTyped(e: KeyEvent?) {
        }

        override fun keyReleased(e: KeyEvent?) {
            if (e != null) {
                if (e.keyCode == KeyEvent.VK_DOWN || e.keyCode == KeyEvent.VK_UP) {
                    parent.codeSelected(e.source as EventCodeList)
                }
            }
        }

        override fun keyPressed(e: KeyEvent?) {
        }
    }

    private class EventCodeListMouseListener(private val parent:EventCodeList): MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
                if (e != null && e.clickCount == 1) {
                    parent.codeSelected(e.source as EventCodeList)
                }
        }
    }

    private fun codeSelected(eList: EventCodeList) {
        if (!eList.valueIsAdjusting) {
            val tagger = eList.tagger
            // creating new one
            val curField = tagger.fieldList.selectedItem.toString()
            val selected = eList.selectedValue
            println("Field level $selected selected")

            // create new HedTagInput pertaining to the curField-curCode pair
            val hedTagInput = HedTagInput(tagger, curField, selected)

            eList.prevSelected = selected

            // hide search result pane
            tagger.hideSearchResultPane()
        }
    }

    /**
     * Add new definition
     */
    fun addDefinition(definition: String) {
        if (!isValidDefName(definition)) {
            tagger.showErrorMessage("Invalid definition name. Definition name should not contain spaces or special characters.")
            return
        }
        // add definition to model and GUI
        if (!tagger.fieldList.hasField("hed_definitions")) {
            tagger.fieldList.addField("hed_definitions")
        }

        // Transfer definition name to definition code automatically
        val defCode = getValidDefinitionCode(definition)
        if (tagger.fieldList.hasCode("hed_definitions", defCode)) {
            tagger.showErrorMessage("Definition $definition already exists")
        } else {
            tagger.fieldList.addDefinition(definition)
        }

        tagger.fieldList.selectedItem = "hed_definitions"
        codeSet = tagger.fieldList.fieldAndUniqueCodeMap["hed_definitions"]!!
        // refresh GUI
        SwingUtilities.invokeLater{
            tagger.fieldList.repaint()
            repaint()
        }
    }

    /**
     * Check if definition name is valid
     */
    private fun isValidDefName(definition: String): Boolean {
        if (definition.contains(" ") || definition.contains("\\") || definition.contains("/")) {
            return false
        }
        return true
    }

    /**
     * Get valid definition code
     */
    fun getValidDefinitionCode(definition: String): String {
        val defCode = definition.replace("-", "_") + "_def"
        return defCode
    }
}
