package works.szabope.plugins.mypy.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import com.intellij.platform.workspace.jps.entities.contentRoot
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.text.nullize
import com.jetbrains.python.sdk.PythonExecuteUtils
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.run.Exclusions
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyArgs
import works.szabope.plugins.mypy.MypyBundle
import java.nio.file.Path
import kotlin.io.path.pathString

class MypyExecutor(private val project: Project) {

    fun execute(configuration: ImmutableSettingsData, parameters: List<String> = emptyList()): ProcessOutput {
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
        }
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
            Exclusions(project).findAll(targets).mapNotNull { getRelativePathFromContentRoot(it)?.toCanonicalPath() }
                .forEach { params.add("--exclude"); params.add(it) }
        }
        params.addAll(extraArgs)
        targets.map { requireNotNull(it.canonicalPath) }.let { params.addAll(it) }
        params
    }

    // mypy's `--exclude` doesn't work with absolute paths
    private fun getRelativePathFromContentRoot(excludeUrlEntity: ExcludeUrlEntity): Path? {
        val contentRootPath =
            excludeUrlEntity.contentRoot?.url?.virtualFile?.path?.let { kotlin.io.path.Path(it) } ?: return null
        val exclusionPath = excludeUrlEntity.url.virtualFile?.path?.let { kotlin.io.path.Path(it) } ?: return null
        return contentRootPath.relativize(exclusionPath)
    }
}