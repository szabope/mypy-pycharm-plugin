package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import works.szabope.plugins.mypy.services.ExecutableService
import works.szabope.plugins.mypy.services.MypySettings

class AutodetectMypyAction : DumbAwareAction() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val deferredPathToExecutable = ExecutableService.getInstance(project).findExecutable()
        deferredPathToExecutable.invokeOnCompletion { throwable ->
            if (throwable == null) {
                deferredPathToExecutable.getCompleted()?.let { MypySettings.getInstance(project).executablePath = it }
            }
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.AutodetectMypyAction"
    }
}