package works.szabope.plugins.mypy.action

import com.intellij.openapi.project.Project
import works.szabope.plugins.mypy.services.AsyncScanService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.SettingsValidator

object ScanActionUtil {
    fun isReadyToScan(project: Project): Boolean {
        return MypySettings.getInstance(project)
            .let { SettingsValidator(project).isComplete(it.getData()) } && !AsyncScanService.getInstance(project).scanInProgress
    }
}
