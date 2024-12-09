package works.szabope.plugins.mypy.actions

import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings

abstract class AbstractScanAction : DumbAwareAction() {
    protected fun isReadyToScan(project: Project): Boolean {
        return MypySettings.getInstance(project).isComplete() && !MypyService.getInstance(project).scanInProgress
    }
}
