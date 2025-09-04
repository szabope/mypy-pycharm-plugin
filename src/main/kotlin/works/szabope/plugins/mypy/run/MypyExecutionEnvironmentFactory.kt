package works.szabope.plugins.mypy.run

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.text.nullize
import works.szabope.plugins.common.run.CliExecutionEnvironmentFactory
import works.szabope.plugins.common.run.Exclusions
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyArgs

class MypyExecutionEnvironmentFactory(private val project: Project) {
    fun createEnvironment(
        configuration: ImmutableSettingsData, targets: Collection<VirtualFile>
    ) = if (configuration.useProjectSdk) {
        val parameters = buildScriptParameters(configuration, targets)
        MypySdkExecutionEnvironmentFactory(project).createEnvironment(configuration, parameters)
    } else {
        val parameters = buildScriptParameters(configuration, targets)
        val command = "${ParametersListUtil.escape(configuration.executablePath!!)} $parameters"
        CliExecutionEnvironmentFactory(project).createEnvironment(command)
    }

    private fun buildScriptParameters(
        configuration: ImmutableSettingsData, targets: Collection<VirtualFile>, vararg extraArgs: String
    ) = with(configuration) {
        val sb = StringBuilder()
        configFilePath.nullize(true)?.let { ParametersListUtil.escape(it) }?.let { sb.append(" --config-file $it") }
        arguments.nullize(true)?.let { ParametersListUtil.escape(it) }?.let { sb.append(" $it") }
        if (excludeNonProjectFiles) {
            Exclusions(project).findAll(targets).joinToString(",").nullize()
                ?.let { sb.append(" --ignore-paths \"$it\"") }
        }
        extraArgs.joinToString(" ").nullize(true)?.let { sb.append(" $it") }
        // in case of duplicated arguments, latter one wins
        sb.append(" ").append(MypyArgs.MYPY_MANDATORY_COMMAND_ARGS).append(" ")
        targets.joinToString(" ") { "\"${it.canonicalPath}\"" }.apply { sb.append(this) }
        sb.toString()
    }
}
