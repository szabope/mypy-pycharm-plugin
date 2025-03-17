package works.szabope.plugins.mypy.toolWindow

import com.intellij.ide.ActivityTracker
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.AutoScrollToSourceHandler
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.EditSourceOnEnterKeyHandler
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.parser.MypyOutput
import java.awt.BorderLayout
import javax.swing.Box
import kotlin.io.path.Path

class MypyToolWindowPanel(private val project: Project, private val tree: Tree = Tree()) :
    SimpleToolWindowPanel(false, true) {

    private val displayedSeverityLevels = MYPY_SEVERITY_FILTER_VALUES.toMutableSet()
    private val treeManager = MypyTreeModelManager(displayedSeverityLevels)
    private val treeExpander = DefaultTreeExpander(tree)

    init {
        treeManager.install(tree)
        border = JBUI.Borders.empty(1)
        add(JBScrollPane(tree), BorderLayout.CENTER)
        addToolbar()
        EditSourceOnDoubleClickHandler.install(tree)
        EditSourceOnEnterKeyHandler.install(tree)
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree)
        TreeUIHelper.getInstance().installSmartExpander(tree)
    }

    override fun uiDataSnapshot(sink: DataSink) {
        sink[MYPY_PANEL_DATA_KEY] = this
        sink.lazy(CommonDataKeys.NAVIGATABLE) {
            val userObject = TreeUtil.getLastUserObject(tree.selectionPath) as? IssueNodeUserObject? ?: return@lazy null
            val file = VfsUtil.findFile(Path(userObject.file), true) ?: return@lazy null
            OpenFileDescriptor(project, file, userObject.line, userObject.column)
        }
        super.uiDataSnapshot(sink)
    }

    fun expandAll() = treeExpander.expandAll()
    fun collapseAll() = treeExpander.collapseAll()

    fun initializeResultTree(targets: List<String>) {
        treeManager.reinitialize(targets)
    }

    fun addScanResult(scanResult: MypyOutput) {
        treeManager.add(scanResult)
        repaint()
        ActivityTracker.getInstance().inc()
    }

    fun isSeverityLevelDisplayed(severityLevel: String): Boolean {
        return displayedSeverityLevels.contains(severityLevel)
    }

    fun setSeverityLevelDisplayed(severityLevel: String, isDisplayed: Boolean) {
        val hadEffect = if (isDisplayed) {
            displayedSeverityLevels.add(severityLevel)
        } else {
            displayedSeverityLevels.remove(severityLevel)
        }
        if (hadEffect) {
            treeManager.reload()
        }
    }

    fun getScanTargets(): List<String> {
        return treeManager.getRootScanPaths()
    }

    private fun addToolbar() {
        val autoScrollToSourceHandler = object : AutoScrollToSourceHandler() {
            override fun isAutoScrollMode() = MypySettings.getInstance(project).isAutoScrollToSource

            override fun setAutoScrollMode(state: Boolean) {
                MypySettings.getInstance(project).isAutoScrollToSource = state
            }
        }
        autoScrollToSourceHandler.install(tree)
        val actionManager = ActionManager.getInstance()
        actionManager.replaceAction("MyPyScrollToSourceAction", autoScrollToSourceHandler.createToggleAction())
        val mainActionGroup = actionManager.getAction(MAIN_ACTION_GROUP) as ActionGroup

        val mainToolbar = ActionManager.getInstance().createActionToolbar(
            ID, mainActionGroup, false
        )
        mainToolbar.targetComponent = this
        val toolBarBox = Box.createHorizontalBox()
        toolBarBox.add(mainToolbar.component)
        add(toolBarBox, BorderLayout.WEST)
    }

    companion object {
        private const val MAIN_ACTION_GROUP: String = "MyPyPluginActions"

        @JvmStatic
        val MYPY_PANEL_DATA_KEY: DataKey<MypyToolWindowPanel> = DataKey.create("MypyToolWindowPanel")

        @JvmStatic
        val MYPY_SEVERITY_FILTER_VALUES = arrayOf("ERROR", "WARNING", "NOTE")
        const val ID = "Mypy"
    }
}
