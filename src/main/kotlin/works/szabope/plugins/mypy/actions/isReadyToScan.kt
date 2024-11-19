package works.szabope.plugins.mypy.actions

import com.intellij.openapi.project.Project
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings

fun isReadyToScan(project: Project): Boolean {
    return MypySettings.getInstance(project).isInitialized() && !MypyService.getInstance(project).scanInProgress
}