package works.szabope.plugins.mypy.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.NavigationItem
import org.jetbrains.annotations.ApiStatus.Internal
import org.jetbrains.annotations.Nls
import works.szabope.plugins.mypy.services.parser.MypyOutput
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class MypyTreeModel(defaultRootNodeText: String) : DefaultTreeModel(RootNode(defaultRootNodeText, emptyList())) {
    override fun setRoot(root: TreeNode?) {
        require(root is RootNode)
        super.setRoot(root)
    }

    override fun getRoot(): RootNode = super.getRoot() as RootNode

    fun append(newNode: DefaultMutableTreeNode, parentNode: DefaultMutableTreeNode) {
        insertNodeInto(newNode, parentNode, getChildCount(parentNode))
    }

    fun updateRootText(message: @Nls String) {
        getRoot().userObject = message
    }

    fun findFileNode(filePath: String): StringNode? {
        for (child in root.children()) {
            if ((child as StringNode).userObject.equals(filePath)) {
                return child
            }
        }
        return null
    }
}

@Internal
class RootNode(text: String, val targets: List<String>) : DefaultMutableTreeNode(text, true)

@Internal
class StringNode(text: String) : DefaultMutableTreeNode(text, true)

@Internal
class IssueNode(issue: MypyOutput) : DefaultMutableTreeNode(IssueNodeUserObject(issue))

@Internal
class IssueNodeUserObject(issue: MypyOutput) :
    PresentationData(issue.toRepresentation(), null, getIconForSeverity(issue.severity), null), NavigationItem {
    val file = issue.file
    val line = issue.line
    val column = issue.column
    override fun getName(): String? = null
    override fun getPresentation() = this
    override fun toString() = presentableText!!
}

fun MypyOutput.toRepresentation(): String = "$message [$code] ($line:$column) $hint"

fun getIconForSeverity(severity: String): Icon? = when (severity) {
    "ERROR" -> AllIcons.General.Error
    "WARNING" -> AllIcons.General.Warning
    "NOTE" -> AllIcons.General.Note
    else -> null
}
