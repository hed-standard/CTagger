import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.io.StringReader
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.xml.bind.JAXBContext


class SchemaView(val tagger: CTagger) : TreeSelectionListener {
    var tree : JTree
    val frame = JFrame("HED Schema ${tagger.hedVersion}")
    init {
        frame.setSize(1024, 800)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val dim = Toolkit.getDefaultToolkit().screenSize
        frame.setLocation(dim.width / 2 - frame.size.width / 2, dim.height / 2 - frame.size.height / 2)

        val mainPane = frame.contentPane
        mainPane.layout = BorderLayout()
        val root = DefaultMutableTreeNode("HED")
        getHedXmlModel(root)

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
        val node = tree.getLastSelectedPathComponent() as DefaultMutableTreeNode
        val nodeInfo = node.userObject
        if (nodeInfo.toString() != "HED") {
            val hedInputDoc = tagger.hedTagInput.document
            hedInputDoc.insertString(hedInputDoc.length, "${nodeInfo}, ", null)
        }
    }
    // Parse HED XML
    private fun getHedXmlModel(root: DefaultMutableTreeNode) {
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
        populateTagSets(root, hedXmlModel.tags)
    }

    // Add tags recursively
    private fun populateTagSets(parent: DefaultMutableTreeNode, tagSets: Set<TagXmlModel>) {
        for (tagXmlModel: TagXmlModel in tagSets) {
            val curNode = DefaultMutableTreeNode(tagXmlModel.name)
            parent.add(curNode)
            populateTagSets(curNode, tagXmlModel.tags)
        }
    }

    fun hide() { frame.isVisible = false }
    fun show() { frame.isVisible = true }
}

