package works.szabope.plugins.mypy.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.text.nullize
import com.jetbrains.python.sdk.PythonExecuteUtils
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.run.Exclusions
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyArgs
import works.szabope.plugins.mypy.MypyBundle
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.pathString

class MypyExecutor(private val project: Project) {

    fun execute(configuration: ImmutableSettingsData, parameters: List<String> = emptyList()): List<String> {
        return if (configuration.useProjectSdk) {
            val pythonSdk = requireNotNull(project.pythonSdk) {
                thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"))
            }
            PythonExecuteUtils.executePyModuleScript(project, pythonSdk, "mypy", parameters)
        } else {
            val commandLine = GeneralCommandLine()
            commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            commandLine.withWorkingDirectory(configuration.projectDirectory?.let { Path.of(it) })
            commandLine.withExePath(configuration.executablePath)
            commandLine.withParameters(parameters)
            ExecUtil.execAndGetOutput(commandLine)
        }.stdout.lines()
    }

    fun buildMypyParameters(configuration: ImmutableSettingsData, shadowMap: Map<VirtualFile, Path>): List<String> {
        val shadowParameters = shadowMap.flatMap { (shadowedOriginal, shadowCastingOne) ->
            listOf("--shadow-file", shadowedOriginal.path, shadowCastingOne.pathString)
        }
        return buildMypyParameters(configuration, shadowMap.keys, shadowParameters)
    }

    fun buildMypyParameters(
        configuration: ImmutableSettingsData,
        targets: Collection<VirtualFile>,
        extraArgs: Collection<String> = emptyList()
    ) = with(configuration) {
        val params = MypyArgs.MYPY_MANDATORY_COMMAND_ARGS.split(" ").toMutableList()
        configFilePath.nullize(true)?.let { params.add("--config-file"); params.add(it) }
        arguments.nullize(true)?.let { params.addAll(ParametersListUtil.parse(it)) }
        if (excludeNonProjectFiles) {
            Exclusions(project).findAll(targets).joinToString(",").nullize()
                ?.let { params.add("--ignore-paths"); params.add(it) }
        }
        params.addAll(extraArgs)
        targets.map { requireNotNull(it.canonicalPath) }.let { params.addAll(it) }
        params
    }
}