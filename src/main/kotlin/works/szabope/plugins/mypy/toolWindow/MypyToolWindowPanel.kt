package works.szabope.plugins.mypy.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.toolWindow.AbstractToolWindowPanel
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.mypy.services.MypySettings

class MypyToolWindowPanel(private val project: Project, @VisibleForTesting val tree: Tree = Tree()) :
    AbstractToolWindowPanel(project, tree) {

    override val treeService: ITreeService
        get() = MypyTreeService.getInstance(project)
    override val settings: Settings
        get() = MypySettings.getInstance(project)

    init {
        super.init(ID, MAIN_ACTION_GROUP, SCROLL_TO_SOURCE_ID)
    }

    companion object {
        private const val MAIN_ACTION_GROUP: String = "works.szabope.plugins.mypy.MypyPluginActions"
        const val ID = "Mypy "
        const val SCROLL_TO_SOURCE_ID = "works.szabope.plugins.mypy.action.ScrollToSourceAction"
    }
}