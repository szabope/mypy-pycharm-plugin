package works.szabope.plugins.mypy.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.jetbrains.python.run.PythonCommandLineState
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import works.szabope.plugins.common.run.RunContentDescriptorFactory

class MypyRunner private constructor() : AsyncProgramRunner<RunnerSettings>() {

    override fun getRunnerId() = "works.szabope.plugins.mypy.run.MypyRunner"

    override fun canRun(executorId: String, profile: RunProfile) = profile is MypyRunConfiguration

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        val promise: AsyncPromise<RunContentDescriptor?> = AsyncPromise()
        execute(state, (Runnable {
            try {
                val executionResult = if (state is PythonCommandLineState) {
                    state.execute(environment.executor)
                } else {
                    state.execute(environment.executor, this)
                }
                ApplicationManager.getApplication()
                    .invokeLater(
                        { promise.setResult(executionResult?.let(RunContentDescriptorFactory::newFakeDescriptor)) },
                        ModalityState.any()
                    )
            } catch (e: ExecutionException) {
                promise.setError(e)
            }
        }))
        return promise
    }

    private fun execute(profileState: RunProfileState, runnable: Runnable) {
        FileDocumentManager.getInstance().saveAllDocuments()
        if (profileState is PythonCommandLineState) {
            AppExecutorUtil.getAppExecutorService().execute(runnable)
        } else {
            ApplicationManager.getApplication().invokeAndWait(runnable)
        }
    }

    companion object {
        @JvmStatic
        val INSTANCE = MypyRunner()
    }
}
