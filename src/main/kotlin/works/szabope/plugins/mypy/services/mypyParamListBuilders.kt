package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.text.nullize
import works.szabope.plugins.common.run.Exclusions
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.mypy.MypyArgs
import java.nio.file.Path
import kotlin.io.path.pathString

context(project: Project)
fun buildMypyParamList(configuration: ToolExecutorConfiguration, shadowMap: Map<VirtualFile, Path>): List<String> {
    val shadowParameters = shadowMap.flatMap { (shadowedOriginal, shadowCastingOne) ->
        listOf("--shadow-file", shadowedOriginal.path, shadowCastingOne.pathString)
    }
    return buildMypyParamList(configuration, shadowMap.keys, shadowParameters)
}

context(project: Project)
fun buildMypyParamList(
    configuration: ToolExecutorConfiguration, targets: Collection<VirtualFile>, extraArgs: Collection<String> = emptyList()
) = with(configuration) {
    val params = MypyArgs.MANDATORY_ARGS.toMutableList()
    configFilePath.nullize(true)?.let { params.add("--config-file"); params.add(it) }
    arguments.nullize(true)?.let { params.addAll(ParametersListUtil.parse(it)) }
    if (excludeNonProjectFiles) {
        Exclusions(project).findAll(targets).mapNotNull { getRelativePathFromContentRoot(it, project)?.toCanonicalPath() }
            .forEach { params.add("--exclude"); params.add(it) }
    }
    params.addAll(extraArgs)
    targets.map { requireNotNull(it.canonicalPath) }.let { params.addAll(it) }
    params
}

// mypy's `--exclude` doesn't work with absolute paths
private fun getRelativePathFromContentRoot(excludeUrlEntity: ExcludeUrlEntity, project: Project): Path? {
    val exclusionPath = excludeUrlEntity.url.virtualFile?.path?.let { kotlin.io.path.Path(it) } ?: return null
    val contentRootPath = WorkspaceModel.getInstance(project).currentSnapshot
        .entities(ContentRootEntity::class.java)
        .firstOrNull { contentRoot -> contentRoot.excludedUrls.contains(excludeUrlEntity) }
        ?.url?.virtualFile?.path?.let { kotlin.io.path.Path(it) } ?: return null
    return contentRootPath.relativize(exclusionPath)
}
