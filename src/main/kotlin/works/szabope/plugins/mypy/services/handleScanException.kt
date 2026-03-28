package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.FlowCollector
import works.szabope.plugins.common.run.ToolExecutionTerminatedException
import works.szabope.plugins.common.services.IncompleteConfigurationNotifier
import works.szabope.plugins.common.services.showClickableBalloonError
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

inline fun <reified T> handleScanException(
    project: Project, noinline commandLine: () -> String?, stdErr: StringBuilder,
    notifier: IncompleteConfigurationNotifier, silent: Boolean = false
): suspend FlowCollector<T>.(Throwable) -> Unit = {
    if (it is ToolExecutionTerminatedException) {
        if (!silent) showClickableBalloonError(project, MypyToolWindowPanel.ID, MypyBundle.message("mypy.toolwindow.balloon.external_error")) {
            DialogManager.showToolExecutionErrorDialog(
                commandLine() ?: "", stdErr.toString(), it.exitCode
            )
        }
    } else {
        // Unexpected exception - tool likely gone
        notifier.showWarningBubble(false)
    }
}