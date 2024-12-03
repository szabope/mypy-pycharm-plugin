package works.szabope.plugins.mypy.actions

import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.MypySettings.SettingsValidationException
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

abstract class AbstractScanAction : DumbAwareAction() {
    protected fun isReadyToScan(project: Project): Boolean {
        val settings = MypySettings.getInstance(project)
        try {
            settings.ensureValid()
        } catch (e: SettingsValidationException) {
            ToolWindowManager.getInstance(project).notifyByBalloon(
                MypyToolWindowPanel.ID,
                MessageType.WARNING,
                MyBundle.message("mypy.toolwindow.balloon.error", e.message!!, e.blame)
            )
        }
        return settings.isInitialized() && !MypyService.getInstance(project).scanInProgress
    }

    protected fun getScanFailureHandler(project: Project) = fun(command: String, status: Int?, error: String) {
        // can't rely on mypy status code https://github.com/python/mypy/issues/6003
        if (error.isNotBlank()) {
            ToolWindowManager.getInstance(project).notifyByBalloon(
                MypyToolWindowPanel.ID, MessageType.ERROR, MyBundle.message(
                    "mypy.error.stderr", command, status ?: 0, error
                )
            )
        }
    }
}