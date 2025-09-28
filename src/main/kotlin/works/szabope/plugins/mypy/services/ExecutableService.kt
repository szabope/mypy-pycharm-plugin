package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import works.szabope.plugins.common.run.CliExecutionEnvironmentFactory
import works.szabope.plugins.common.run.ProcessException
import works.szabope.plugins.common.run.execute
import works.szabope.plugins.mypy.MypyBundle

@Service(Service.Level.PROJECT)
class ExecutableService(private val project: Project, private val cs: CoroutineScope) {

    fun findExecutable(): Deferred<@NlsSafe String?> {
        val commandAndArgs = if (SystemInfo.isWindows) {
            Pair("where.exe", listOf("mypy.exe"))
        } else {
            Pair("which", listOf("mypy"))
        }
        val environment = CliExecutionEnvironmentFactory(project).createEnvironment(
            commandAndArgs.first, commandAndArgs.second
        )
        val stdOutFlow = execute(environment).catch {
            if (it is ProcessException && it.exitCode == 1) {
                // ran but not found in PATH
            }
            thisLogger().debug(MypyBundle.message("mypy.autodetect.failed", commandAndArgs), it)
        }
        return cs.async {
            stdOutFlow.firstOrNull()
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ExecutableService = project.service()
    }
}