package works.szabope.plugins.mypy.services.cli

import com.intellij.openapi.project.Project
import com.intellij.python.terminal.PyVirtualEnvTerminalCustomizer
import com.intellij.util.EnvironmentUtil
import kotlinx.coroutines.flow.Flow

class PythonEnvironmentAwareCli(private val project: Project) {

    suspend fun execute(
        vararg command: String,
        workDir: String? = null,
        handle: suspend (Flow<String>) -> Unit
    ): Cli.Status {
        require(command.isNotEmpty())
        val environment = getEnvironment().toMutableMap()
        val environmentAwareCommand = PyVirtualEnvTerminalCustomizer().customizeCommandAndEnvironment(
            project, project.basePath, command, environment
        ).filter { it.isNotEmpty() }.toTypedArray()
        return Cli.execute(
            command = environmentAwareCommand,
            workDir = workDir,
            env = environment
        ) { handle(it) }
    }

    private fun getEnvironment(): Map<String, String> {
        val envs = HashMap(System.getenv())
        envs[EnvironmentUtil.DISABLE_OMZ_AUTO_UPDATE] = "true"
        envs["HISTFILE"] = "/dev/null"
        return envs
    }
}
