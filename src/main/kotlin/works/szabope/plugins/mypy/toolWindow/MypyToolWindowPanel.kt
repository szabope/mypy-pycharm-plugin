package works.szabope.plugins.mypy.toolWindow

import com.intellij.ide.ActivityTracker
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys.BGT_DATA_PROVIDER
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
import works.szabope.plugins.mypy.services.cli.MypyOutput
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

    override fun getData(dataId: String): Any? {
        if (MYPY_PANEL_DATA_KEY.`is`(dataId)) {
            return this
        }
        if (BGT_DATA_PROVIDER.`is`(dataId)) {
            val userObject = TreeUtil.getLastUserObject(tree.selectionPath) as? IssueNodeUserObject? ?: return null
            val superProvider = super.getData(dataId) as DataProvider?
            return CompositeDataProvider.compose({ slowId -> getSlowData(slowId, userObject) }, superProvider)
        }

        return super.getData(dataId)
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
        actionManager.replaceAction("MypyScrollToSourceAction", autoScrollToSourceHandler.createToggleAction())
        val mainActionGroup = actionManager.getAction(MAIN_ACTION_GROUP) as ActionGroup

        val mainToolbar = ActionManager.getInstance().createActionToolbar(
            ID, mainActionGroup, false
        )
        mainToolbar.targetComponent = this
        val toolBarBox = Box.createHorizontalBox()
        toolBarBox.add(mainToolbar.component)
        add(toolBarBox, BorderLayout.WEST)
    }

    private fun getSlowData(slowId: String, userObject: IssueNodeUserObject): Any? {
        if (CommonDataKeys.NAVIGATABLE.`is`(slowId)) {
            val file = VfsUtil.findFile(Path(userObject.file), true) ?: return null
            return OpenFileDescriptor(project, file, userObject.line, userObject.column)
        }
        return null
    }

    companion object {
        private const val MAIN_ACTION_GROUP: String = "MyPyPluginActions"

        @JvmStatic
        val MYPY_PANEL_DATA_KEY: DataKey<MypyToolWindowPanel> = DataKey.create("MypyToolWindowPanel")

        @JvmStatic
        val MYPY_SEVERITY_FILTER_VALUES = arrayOf("ERROR", "WARNING", "NOTE")
        const val ID = "MyPy"
    }
}
