import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Toolkit
import java.io.StringReader
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.xml.bind.JAXBContext


class SchemaView(private val tagger: CTagger, hedRoot: TagModel) : TreeSelectionListener {
    var tree : JTree
    val frame = JFrame("HED Schema ${tagger.hedVersion}")
    init {
        frame.setSize(1024, 800)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        frame.setLocation(dim.width / 2 - frame.size.width / 2, dim.height / 2 - frame.size.height / 2)

        val mainPane = frame.contentPane
        mainPane.layout = BorderLayout()

        val prompt = JLabel("Click on a tag to add to the annotation")
        prompt.border = EmptyBorder(10,10,10,10)
        prompt.font = Font("San Serif", Font.BOLD, 12)
        prompt.foreground = Style.BLUE_DARK
        mainPane.add(prompt, BorderLayout.PAGE_START)
        val root = DefaultMutableTreeNode("HED")
        populateTagSets(root, hedRoot.children)

        tree = JTree(root)
        tree.addTreeSelectionListener(this)
        tree.showsRootHandles = true
        val renderer = DefaultTreeCellRenderer()
        renderer.leafIcon = null
        renderer.openIcon = null
        renderer.closedIcon = null
        renderer.openIcon = null
        tree.cellRenderer = renderer
        val treeView = JScrollPane(tree)
        treeView.preferredSize = Dimension(250, 400)
        mainPane.add(treeView, BorderLayout.CENTER)
        frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        frame.pack()
    }

    override fun valueChanged(e: TreeSelectionEvent?) {
        val node = tree.lastSelectedPathComponent as DefaultMutableTreeNode
        val nodeInfo = node.userObject
        if (nodeInfo.toString() != "HED") {
            val hedInputDoc = tagger.hedTagInput.document
            hedInputDoc.insertString(tagger.hedTagInput.caretPosition, "${nodeInfo}, ", null)
        }
    }

    // Add tags to tree recursively
    private fun populateTagSets(parent: DefaultMutableTreeNode, tagSets: List<TagModel>) {
        for (tagModel: TagModel in tagSets) {
            val curNode = DefaultMutableTreeNode(tagModel.name)
            parent.add(curNode)
            populateTagSets(curNode, tagModel.children)
        }
    }

    fun hide() { frame.isVisible = false }
    fun show() { frame.isVisible = true }
}

