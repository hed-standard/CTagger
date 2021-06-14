import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreePath


class SchemaView(private val tagger: CTagger, hedRoot: TagModel) : TreeSelectionListener {
    var tree : JTree
    var nodeDescription: HashMap<String, String> = HashMap()
    lateinit var infoPane: JTextArea
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
        var ml: MouseListener = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val selRow = tree.getRowForLocation(e.getX(), e.getY())
                if (selRow != -1) {
                    val selPath: TreePath = tree.getPathForLocation(e.getX(), e.getY())
                    if (e.getClickCount() === 1) {
                        mySingleClick(selRow, selPath)
                    } else if (e.getClickCount() === 2) {
                        myDoubleClick(selRow, selPath)
                    }
                }
            }
        }
//        tree.addTreeSelectionListener(this)
        tree.addMouseListener(ml)
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

        infoPane = JTextArea(4,10)
        infoPane.lineWrap = true
        infoPane.wrapStyleWord = true
        infoPane.isEditable = false
        val infoView = JScrollPane(infoPane)
        infoView.preferredSize = Dimension(250, 400)
        mainPane.add(infoView, BorderLayout.EAST)

        frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        frame.pack()
    }

    fun mySingleClick(selRow:Int, selPath: TreePath) {
        val node = tree.lastSelectedPathComponent as DefaultMutableTreeNode
        val nodeInfo = node.userObject
        if (nodeInfo.toString() != "HED") {
            infoPane.text = nodeDescription[nodeInfo.toString()]
        }
    }
    fun myDoubleClick(selRow:Int, selPath: TreePath) {
        val node = tree.lastSelectedPathComponent as DefaultMutableTreeNode
        val nodeInfo = node.userObject
        if (nodeInfo.toString() != "HED") {
            infoPane.text = nodeDescription[nodeInfo.toString()]
            val hedInputDoc = tagger.hedTagInput.document
            hedInputDoc.insertString(tagger.hedTagInput.caretPosition, "${if (nodeInfo.toString() == "#") node.parent.toString()+"/" else nodeInfo}, ", null)
        }
    }
    override fun valueChanged(e: TreeSelectionEvent?) {
        val node = tree.lastSelectedPathComponent as DefaultMutableTreeNode
        val nodeInfo = node.userObject
        if (nodeInfo.toString() != "HED") {
            infoPane.text = nodeDescription[nodeInfo.toString()]
            val hedInputDoc = tagger.hedTagInput.document
            hedInputDoc.insertString(tagger.hedTagInput.caretPosition, "${if (nodeInfo.toString() == "#") node.parent.toString()+"/" else nodeInfo}, ", null)
        }
    }

    // Add tags to tree recursively
    private fun populateTagSets(parent: DefaultMutableTreeNode, tagSets: List<TagModel>) {
        for (tagModel: TagModel in tagSets) {
            nodeDescription[tagModel.name!!] = tagModel.description!!
            val curNode = DefaultMutableTreeNode(tagModel.name)
            parent.add(curNode)
            populateTagSets(curNode, tagModel.children)
        }
    }

    fun hide() { frame.isVisible = false }
    fun show() { frame.isVisible = true }
}

