package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.jetbrains.python.sdk.pythonSdk
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.common.services.BasicSettingsData
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.Settings

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
        set(value) {
            // workaround for string() normalizes empty string to null
            state.mypyExecutable = value.ifBlank { " " }
        }

    override var configFilePath
        get() = state.configFilePath?.trim() ?: ""
        set(value) {
            // workaround for string() normalizes empty string to null
            state.configFilePath = value.ifBlank { " " }
        }

    override var arguments
        get() = state.arguments?.trim() ?: ""
        set(value) {
            // workaround for string() normalizes empty string to null
            state.arguments = value.ifBlank { " " }
        }

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
        if (state.mypyExecutable == null && oldSettings.executablePath != null) {
            executablePath = oldSettings.executablePath!!
        }
        if (executablePath.isNotBlank() && project.pythonSdk == null) {
            useProjectSdk = false
        }
        if (state.configFilePath == null && oldSettings.configFilePath != null) {
            configFilePath = oldSettings.configFilePath!!
        }
        if (state.arguments == null && oldSettings.arguments != null) {
            arguments = oldSettings.arguments!!
        }
        if (state.projectDirectory == null) {
            workingDirectory = project.guessProjectDir()?.canonicalPath
        }
        initialized = true
    }

    override fun getValidConfiguration(): Result<ImmutableSettingsData> {
        val workingDirectory = workingDirectory
        if (workingDirectory.isNullOrBlank()) {
            return Result.failure(MypySettingsInvalid("Working directory is required"))
        }
        if (!isMypySet()) {
            return Result.failure(MypySettingsInvalid("Mypy tool is not set"))
        }

        return MypyExecutorConfiguration(
            executablePath,
            useProjectSdk,
            configFilePath,
            arguments,
            workingDirectory,
            excludeNonProjectFiles,
            scanBeforeCheckIn
        ).let { Result.success(it) }
    }

    private fun isMypySet(): Boolean {
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
