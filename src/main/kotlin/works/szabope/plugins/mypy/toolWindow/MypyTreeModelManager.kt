package works.szabope.plugins.mypy.toolWindow

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.treeStructure.Tree
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.parser.MypyOutput

class MypyTreeModelManager(private val displayedSeverityLevels: MutableSet<String>) {
    private val issues = mutableSetOf<MypyOutput>()
    private val model = MypyTreeModel(MypyBundle.message("mypy.toolwindow.name.empty"))

    fun add(issue: MypyOutput) {
        issues.add(issue)
        if (isDisplayed(issue)) {
            addToTree(issue)
            thisLogger().debug("Issue added to tree: $issue")
        }
    }

    fun reload() {
        resetRoot()
        issues.filter { isDisplayed(it) }.forEach { addToTree(it) }
    }

    fun reinitialize(targets: List<String>) {
        issues.clear()
        resetRoot(targets)
    }

    fun getRootScanPaths(): List<String> {
        return model.root.targets
    }

    fun install(tree: Tree) {
        tree.model = model
    }

    private fun resetRoot(targetsMaybe: List<String>? = null) {
        val targets = targetsMaybe ?: model.root.targets
        model.setRoot(RootNode(MypyBundle.message("mypy.toolwindow.root.message", 0, 0), targets))
    }

    private fun addToTree(issue: MypyOutput) {
        val fileNode = findOrAddFileNode(issue.file)
        val issueNode = IssueNode(issue)
        model.append(issueNode, fileNode)
        model.updateRootText(
            MypyBundle.message(
                "mypy.toolwindow.root.message", issues.size, model.getChildCount(model.root)
            )
        )
    }

    private fun isDisplayed(issue: MypyOutput): Boolean {
        val result = displayedSeverityLevels.contains(issue.severity)
        if (!result) { // severity filters are turned off since mypy reporting everything as error makes them useless
            thisLogger().warn("Received issue with severity ${issue.severity} which isn't supported.")
        }
        return result
    }

    private fun findOrAddFileNode(file: String): StringNode {
        var fileNode = model.findFileNode(file)
        if (fileNode == null) {
            fileNode = StringNode(file)
            model.append(fileNode, model.root)
        }
        return fileNode
    }
}
