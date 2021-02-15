import java.awt.BorderLayout
import java.awt.Toolkit
import javax.swing.*

fun main() {
    SwingUtilities.invokeLater { TaggerLauncher() }
}
class TaggerLauncher: JFrame() {
    init {
        setSize(300, 500)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        setLocation(dim.width / 2 - size.width / 2, dim.height / 2 - size.height / 2)

        val mainPane = contentPane
        mainPane.layout = BorderLayout()

        mainPane.add(JLabel("Welcome to CTagger!"), BorderLayout.NORTH)

        val importTSVBtn = JButton("Import spreadsheet")
        importTSVBtn.addActionListener {
            val fc = JFileChooser()
            val fileChosen = fc.showOpenDialog(this)
            if (fileChosen == JFileChooser.APPROVE_OPTION) {
                val file = fc.selectedFile.toString()
                SwingUtilities.invokeLater { CTagger(isJson = false, isTSV = true, filename = file, isScratch = false) }
            }
        }
        val importJsonBtn = JButton("Import dictionary")
        importJsonBtn.addActionListener {
            val fc = JFileChooser()
            val fileChosen = fc.showOpenDialog(this)
            if (fileChosen == JFileChooser.APPROVE_OPTION) {
                val file = fc.selectedFile.toString()
                SwingUtilities.invokeLater { CTagger(isJson = true, isTSV = false, filename = file, isScratch = false) }
            }
        }
        val startScratchBtn = JButton("Quick tagging")
        startScratchBtn.addActionListener {
            SwingUtilities.invokeLater { CTagger(isJson = false, isTSV = false, filename = "", isScratch=true) }
        }
        mainPane.add(importTSVBtn, BorderLayout.LINE_START)
        mainPane.add(importJsonBtn, BorderLayout.CENTER)
        mainPane.add(startScratchBtn, BorderLayout.LINE_END)

        background = Style.BLUE_MEDIUM
        mainPane.background = Style.BLUE_MEDIUM
        pack()
        isVisible = true
    }
}