import java.awt.BorderLayout
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

fun main() {
    TaggerLauncher()
}
class TaggerLauncher: JFrame() {
    init {
        title = "Welcome to CTagger"
        defaultCloseOperation = EXIT_ON_CLOSE

        val mainPane = contentPane
        mainPane.layout = BorderLayout()

        val label = JLabel("Choose one of the options below to start tagging", SwingConstants.CENTER)
        label.font = Font("Sans Serif", Font.BOLD, 14)
        label.foreground = Style.BLUE_DARK
        label.border = EmptyBorder(10,10,10,10)
        mainPane.add(label, BorderLayout.NORTH)

        val btnPanel = JPanel()
        val importTSVBtn = JButton("Import BIDS event spreadsheet")
        importTSVBtn.addActionListener {
            val fc = JFileChooser()
            val fileChosen = fc.showOpenDialog(this)
            if (fileChosen == JFileChooser.APPROVE_OPTION) {
                val file = fc.selectedFile.toString()
                SwingUtilities.invokeLater { CTagger(isJson = false, isTSV = true, filename = file, jsonString = "",isScratch = false) }
                isVisible = false
            }
        }
        val importJsonBtn = JButton("Import BIDS event dictionary")
        importJsonBtn.addActionListener {
            val fc = JFileChooser()
            val fileChosen = fc.showOpenDialog(this)
            if (fileChosen == JFileChooser.APPROVE_OPTION) {
                val file = fc.selectedFile.toString()
                SwingUtilities.invokeLater { CTagger(isJson = true, isTSV = false, filename = file, jsonString = "",isScratch = false) }
                isVisible = false
            }
        }
        val startScratchBtn = JButton("Quick tagging")
        startScratchBtn.addActionListener {
            SwingUtilities.invokeLater { CTagger(isJson = false, isTSV = false, filename = "", jsonString = "",isScratch=true) }
            isVisible = false
        }
        btnPanel.add(importTSVBtn)
        btnPanel.add(importJsonBtn)
        btnPanel.add(startScratchBtn)
        btnPanel.border = EmptyBorder(10,10,10,10)
        btnPanel.background = Style.BLUE_MEDIUM

        mainPane.add(btnPanel, BorderLayout.CENTER)

        UIManager.put("Panel.background", Style.BLUE_MEDIUM)
        UIManager.put("OptionPane.background", Style.BLUE_MEDIUM)
        SwingUtilities.updateComponentTreeUI(this)
        background = Style.BLUE_MEDIUM
        mainPane.background = Style.BLUE_MEDIUM
        pack()
        val dim = Toolkit.getDefaultToolkit().screenSize
        setLocation(dim.width / 2 - size.width / 2, dim.height / 2 - size.height / 2)
        isVisible = true
    }

}