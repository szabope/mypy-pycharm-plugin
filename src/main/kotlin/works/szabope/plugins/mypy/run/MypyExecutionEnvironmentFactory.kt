package works.szabope.plugins.mypy.run

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.nullize
import works.szabope.plugins.common.run.CliExecutionEnvironmentFactory
import works.szabope.plugins.common.run.Exclusions
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyArgs
import java.nio.file.Path
import kotlin.io.path.pathString

class MypyExecutionEnvironmentFactory(private val project: Project) {
    fun createEnvironment(configuration: ImmutableSettingsData, targets: Map<VirtualFile, Path>): ExecutionEnvironment {
        val shadowParameters = targets.flatMap { (shadowedOriginal, shadowCastingOne) ->
            listOf("--shadow-file", shadowedOriginal.path, shadowCastingOne.pathString)
        }
        return createEnvironment(configuration, targets.keys, shadowParameters)
    }

    fun createEnvironment(
        configuration: ImmutableSettingsData,
        targets: Collection<VirtualFile>,
        extraArgs: Collection<String> = emptyList()
    ): ExecutionEnvironment {
        val parameters = buildScriptParameters(configuration, targets, extraArgs)
        return if (configuration.useProjectSdk) {
            MypySdkExecutionEnvironmentFactory(project).createEnvironment(parameters, configuration.projectDirectory)
        } else {
            CliExecutionEnvironmentFactory(project).createEnvironment(
                configuration.executablePath!!, parameters, configuration.projectDirectory
            )
        }
    }

    private fun buildScriptParameters(
        configuration: ImmutableSettingsData, targets: Collection<VirtualFile>, extraArgs: Collection<String>
    ) = with(configuration) {
        val params = MypyArgs.MYPY_MANDATORY_COMMAND_ARGS.split(" ").toMutableList()
        configFilePath.nullize(true)?.let { params.add("--config-file"); params.add(it) }
        arguments.nullize(true)?.let { params.add(" $it") }
        if (excludeNonProjectFiles) {
            Exclusions(project).findAll(targets).joinToString(",").nullize()
                ?.let { params.add("--ignore-paths"); params.add(it) }
        }
        params.addAll(extraArgs)
        targets.map { requireNotNull(it.canonicalPath) }.let { params.addAll(it) }
        params
    }
}
