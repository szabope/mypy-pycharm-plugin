package works.szabope.plugins.mypy.services.cli

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.python.terminal.PyVirtualEnvTerminalCustomizer
import com.intellij.util.EnvironmentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PyVirtualEnvCli(private val project: Project) {
    private val logger = logger<PyVirtualEnvCli>()

    data class Status(val resultCode: Int, private val stderrLines: List<String>) {
        val stderr: String
            get() = stderrLines.joinToString("\n")
    }

    suspend fun execute(command: String, workDir: String? = null, stdout: suspend (Flow<String>) -> Unit): Status {
        require(command.isNotBlank())
        val directory = workDir?.let { java.io.File(workDir) }
        val environment = getEnvironment().toMutableMap()
        val environmentAwareCommand = PyVirtualEnvTerminalCustomizer().customizeCommandAndEnvironment(
            project, project.basePath, command.split(" ").toTypedArray(), environment
        ).filter { it.isNotEmpty() }.toTypedArray()
        logger.debug("executing command: ${environmentAwareCommand.joinToString(" ")} with environment: $environment")
        return withContext(Dispatchers.IO) {
            val result = process(
                *environmentAwareCommand,
                stdout = Redirect.Consume { stdout(it) },
                stderr = Redirect.CAPTURE,
                env = environment,
                directory = directory
            )
            Status(result.resultCode, result.output)
        }
    }

    private fun getEnvironment(): Map<String, String> {
        val envs = HashMap(System.getenv())
        envs[EnvironmentUtil.DISABLE_OMZ_AUTO_UPDATE] = "true"
        envs["HISTFILE"] = "/dev/null"
        return envs
    }
}
