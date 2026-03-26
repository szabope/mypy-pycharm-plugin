package works.szabope.plugins.mypy.action

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.action.AbstractScanAction
import works.szabope.plugins.common.action.AbstractScanJobRegistry
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.mypy.services.AsyncScanService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.parser.MypyMessageConverter
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

open class ScanAction : AbstractScanAction() {

    override fun getTreeService(project: Project): ITreeService = MypyTreeService.getInstance(project)
    override fun getSettings(project: Project): Settings = MypySettings.getInstance(project)
    override fun getScanJobRegistry(project: Project): AbstractScanJobRegistry = MypyScanJobRegistryService.getInstance(project)
    override fun getToolWindowId(): String = MypyToolWindowPanel.ID

    override suspend fun scanAndAdd(
        project: Project,
        targets: Collection<VirtualFile>,
        configuration: ToolExecutorConfiguration,
        treeService: ITreeService
    ) {
        AsyncScanService.getInstance(project).scan(targets, configuration).forEach {
            val mypyMessage = MypyMessageConverter.convert(it)
            withContext(Dispatchers.EDT) {
                treeService.add(mypyMessage)
            }
        }
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.ScanAction"
    }
}
