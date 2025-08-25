package works.szabope.plugins.mypy.services

import com.intellij.collaboration.util.ResultUtil.processErrorAndGet
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import works.szabope.plugins.common.run.CliExecutionEnvironmentFactory
import works.szabope.plugins.common.run.ProcessException
import works.szabope.plugins.common.run.execute
import works.szabope.plugins.mypy.MypyBundle

@Service(Service.Level.PROJECT)
class ExecutableService(private val project: Project) {

    fun findExecutable(): String? {
        val locateCommand = if (SystemInfo.isWindows) "where.exe mypy.exe" else "which mypy"
        val environment = CliExecutionEnvironmentFactory(project).createEnvironment(locateCommand)
        return runBlockingCancellable {
            execute(environment).mapCatching { it ->
                val stdout = buildString { it.collect { appendLine(it) } }
                return@mapCatching stdout.lines().first()
            }
        }.processErrorAndGet { error ->
            if (error is ProcessException && error.exitCode == 1) {
                // ran but not found in PATH
                return null
            }
            thisLogger().debug(MypyBundle.message("mypy.autodetect.failed", locateCommand), error)
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ExecutableService = project.service()
    }
}