package works.szabope.plugins.mypy.services

import com.intellij.collaboration.util.ResultUtil.processErrorAndGet
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import kotlinx.coroutines.flow.first
import works.szabope.plugins.common.run.CliExecutionEnvironmentFactory
import works.szabope.plugins.common.run.ProcessException
import works.szabope.plugins.common.run.execute
import works.szabope.plugins.mypy.MypyBundle

@Service(Service.Level.PROJECT)
class ExecutableService(private val project: Project) {

    fun findExecutable(): String? {
        val commandAndArgs = if (SystemInfo.isWindows) {
            Pair("where.exe", listOf("mypy.exe"))
        } else {
            Pair("which", listOf("mypy"))
        }
        val environment = CliExecutionEnvironmentFactory(project).createEnvironment(
            commandAndArgs.first, commandAndArgs.second
        )
        return runBlockingCancellable {
            execute(environment).runCatching { first() }
        }.processErrorAndGet {
            if (it is ProcessException && it.exitCode == 1) {
                // ran but not found in PATH
                return null
            }
            thisLogger().debug(MypyBundle.message("mypy.autodetect.failed", commandAndArgs), it)
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ExecutableService = project.service()
    }
}