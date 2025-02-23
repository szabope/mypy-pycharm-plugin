package works.szabope.plugins.mypy.services.cli

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

object Cli {
    class Status(val resultCode: Int, private val stderrLines: List<String>) {
        val stderr: String
            get() = stderrLines.joinToString("\n")
    }

    suspend fun execute(
        vararg command: String,
        workDir: String? = null,
        env: Map<String, String>? = null,
        stdout: suspend (Flow<String>) -> Unit
    ): Status {
        require(command.isNotEmpty())
        val directory = workDir?.let { java.io.File(workDir) }
        return withContext(Dispatchers.IO) {
            val result = process(
                command = command,
                stdout = Redirect.Consume { stdout(it) },
                stderr = Redirect.CAPTURE,
                directory = directory,
                env = env
            )
            Status(result.resultCode, result.output)
        }
    }
}
