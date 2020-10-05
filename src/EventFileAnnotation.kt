import java.awt.Dimension
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable

class EventFileAnnotation(frame: JFrame, val tagger: CTagger) : JDialog(frame) {
    init {
        this.setPreferredSize(Dimension(500, 500))
        var columnNames = tagger.fieldMap.keys
        columnNames.add("HED tags")
        val table = JTable(tagger.eventFile, columnNames.toTypedArray())
        table.setPreferredScrollableViewportSize(Dimension(500, 70))
        table.setFillsViewportHeight(true)
        table.showHorizontalLines = true
        val scrollPane = JScrollPane(table)
        this.add(scrollPane)
        this.isVisible = false
    }
}