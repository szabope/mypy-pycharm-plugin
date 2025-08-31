package works.szabope.plugins.mypy.toolWindow

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.toolWindow.AbstractTreeService
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.mypy.services.mypySeverityConfigs

@Service(Service.Level.PROJECT)
class MypyTreeService : AbstractTreeService(mypySeverityConfigs.keys) {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): ITreeService = project.service<MypyTreeService>()
    }
}