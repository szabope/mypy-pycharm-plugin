package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.FlowCollector
import works.szabope.plugins.common.run.ToolExecutionTerminatedException
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager

inline fun <reified T> handleScanException(
    project: Project, configuration: ImmutableSettingsData, stdErr: StringBuilder
): suspend FlowCollector<T>.(Throwable) -> Unit = {
    if (it is ToolExecutionTerminatedException) {
        showClickableBalloonError(project, MypyBundle.message("mypy.toolwindow.balloon.external_error")) {
            DialogManager.showToolExecutionErrorDialog(
                configuration, stdErr.toString(), it.exitCode
            )
        }
    } else {
        // Unexpected exception
        showClickableBalloonError(
            project, MypyBundle.message("mypy.toolwindow.balloon.failed_to_execute")
        ) {
            DialogManager.showFailedToExecuteErrorDialog(
                it.message ?: MypyBundle.message("mypy.please_report_this_issue")
            )
        }
    }
}