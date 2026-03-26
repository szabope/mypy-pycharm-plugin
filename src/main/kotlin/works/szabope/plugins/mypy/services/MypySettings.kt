package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.jetbrains.python.sdk.pythonSdk
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.common.blankToSingleSpace
import works.szabope.plugins.common.services.BasicSettingsData
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.services.ToolSettingsInvalidException

@Service(Service.Level.PROJECT)
@State(name = "MypySettings", storages = [Storage("MypyPlugin.xml")], category = SettingsCategory.PLUGINS)
class MypySettings(internal val project: Project) : SimplePersistentStateComponent<MypySettings.MypyState>(MypyState()),
    Settings {

    private var initialized = false

    class MypyState : BaseState() {
        var mypyExecutable by string()
        var useProjectSdk by property(true)
        var configFilePath by string()
        var arguments by string()
        var autoScrollToSource by property(false)
        var excludeNonProjectFiles by property(true)
        var projectDirectory by string()
        var scanBeforeCheckIn by property(false)
    }

    override var useProjectSdk
        get() = state.useProjectSdk
        set(value) {
            state.useProjectSdk = value
        }

    override var executablePath
        get() = state.mypyExecutable?.trim() ?: ""
        set(value) { state.mypyExecutable = value.blankToSingleSpace() }

    override var configFilePath
        get() = state.configFilePath?.trim() ?: ""
        set(value) { state.configFilePath = value.blankToSingleSpace() }

    override var arguments
        get() = state.arguments?.trim() ?: ""
        set(value) { state.arguments = value.blankToSingleSpace() }

    override var isAutoScrollToSource
        get() = state.autoScrollToSource
        set(value) {
            state.autoScrollToSource = value
        }

    override var excludeNonProjectFiles
        get() = state.excludeNonProjectFiles
        set(value) {
            state.excludeNonProjectFiles = value
        }

    override var workingDirectory
        get() = state.projectDirectory
        set(value) {
            state.projectDirectory = value
        }

    override var scanBeforeCheckIn
        get() = state.scanBeforeCheckIn
        set(value) {
            state.scanBeforeCheckIn = value
        }

    override suspend fun initSettings(oldSettings: BasicSettingsData) {
        if (state.mypyExecutable == null) {
            oldSettings.executablePath?.let { executablePath = it }
        }
        if (executablePath.isNotBlank() && project.pythonSdk == null) {
            useProjectSdk = false
        }
        if (state.configFilePath == null) {
            oldSettings.configFilePath?.let { configFilePath = it }
        }
        if (state.arguments == null) {
            oldSettings.arguments?.let { arguments = it }
        }
        if (state.projectDirectory == null) {
            workingDirectory = project.guessProjectDir()?.canonicalPath
        }
        initialized = true
    }

    override suspend fun getValidConfiguration(): Result<ToolExecutorConfiguration> {
        val workingDirectory = workingDirectory
        if (workingDirectory.isNullOrBlank()) {
            return Result.failure(ToolSettingsInvalidException("Working directory is required"))
        }
        if (!isToolSet()) {
            return Result.failure(ToolSettingsInvalidException("Mypy tool is not set"))
        }

        return ToolExecutorConfiguration(
            executablePath,
            useProjectSdk,
            configFilePath,
            arguments,
            workingDirectory,
            excludeNonProjectFiles,
            scanBeforeCheckIn
        ).let { Result.success(it) }
    }

    private suspend fun isToolSet(): Boolean {
        return if (useProjectSdk) {
            project.pythonSdk != null && MypyPluginPackageManagementService.getInstance(project)
                .checkInstalledRequirement().isSuccess
        } else {
            executablePath.isNotBlank()
        }
    }

    @TestOnly
    fun reset() {
        loadState(MypyState())
    }

    fun isInitialized() = initialized

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypySettings = project.service()
    }
}
