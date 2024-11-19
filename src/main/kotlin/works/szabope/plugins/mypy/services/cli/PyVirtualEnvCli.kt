package works.szabope.plugins.mypy.services.cli

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import com.intellij.openapi.project.Project
import com.intellij.python.terminal.PyVirtualEnvTerminalCustomizer
import com.intellij.util.EnvironmentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PyVirtualEnvCli(private val project: Project) {

    suspend fun execute(command: String, stdout: suspend (Flow<String>) -> Unit) {
        require(command.isNotBlank())
        val environment = getEnvironment().toMutableMap()
        val environmentAwareCommand = PyVirtualEnvTerminalCustomizer().customizeCommandAndEnvironment(
            project, project.basePath, command.split(" ").toTypedArray(), environment
        ).filter { it.isNotEmpty() }.toTypedArray()
        withContext(Dispatchers.IO) {
            process(
                *environmentAwareCommand, stdout = Redirect.Consume { stdout(it) }, env = environment
            )
        }
    }

    private fun getEnvironment(): Map<String, String> {
        val envs = HashMap(System.getenv())
        envs[EnvironmentUtil.DISABLE_OMZ_AUTO_UPDATE] = "true"
        envs["HISTFILE"] = "/dev/null"
        return envs
    }
}
