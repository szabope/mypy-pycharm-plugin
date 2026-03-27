package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.FlowCollector
import works.szabope.plugins.common.run.ToolExecutionTerminatedException
import works.szabope.plugins.common.services.IncompleteConfigurationNotifier
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.showClickableBalloonError
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

inline fun <reified T> handleScanException(
    project: Project, configuration: ToolExecutorConfiguration, stdErr: StringBuilder,
    notifier: IncompleteConfigurationNotifier
): suspend FlowCollector<T>.(Throwable) -> Unit = {
    if (it is ToolExecutionTerminatedException) {
        showClickableBalloonError(project, MypyToolWindowPanel.ID, MypyBundle.message("mypy.toolwindow.balloon.external_error")) {
            DialogManager.showToolExecutionErrorDialog(
                configuration, stdErr.toString(), it.exitCode
            )
        }
    } else {
        // Unexpected exception - tool likely gone
        notifier.showWarningBubble(false)
    }
}