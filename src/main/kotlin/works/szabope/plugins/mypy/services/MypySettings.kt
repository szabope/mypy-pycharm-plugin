package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.jetbrains.python.sdk.pythonSdk
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.common.services.BasicSettingsData
import works.szabope.plugins.common.services.Settings

@Service(Service.Level.PROJECT)
@State(name = "MypySettings", storages = [Storage("MypyPlugin.xml")], category = SettingsCategory.PLUGINS)
class MypySettings(internal val project: Project) :
    SimplePersistentStateComponent<MypySettings.MypyState>(MypyState()), Settings {

    @ApiStatus.Internal
    class MypyState : BaseState() {
        var initialized by property(false)
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
        get() = state.mypyExecutable
        set(value) {
            state.mypyExecutable = value
        }

    override var configFilePath
        get() = state.configFilePath
        set(value) {
            state.configFilePath = value
        }

    override var arguments
        get() = state.arguments
        set(value) {
            state.arguments = value
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

    override var projectDirectory
        get() = state.projectDirectory
        set(value) {
            state.projectDirectory = value
        }

    override var scanBeforeCheckIn
        get() = state.scanBeforeCheckIn
        set(value) {
            state.scanBeforeCheckIn = value
        }

    override suspend fun initSettings(oldSettings: BasicSettingsData?) {
        if (state.initialized) return
        if (oldSettings != null) {
            executablePath = oldSettings.executablePath
            if (executablePath != null && project.pythonSdk == null) {
                useProjectSdk = false
            }
            configFilePath = oldSettings.configFilePath
            arguments = oldSettings.arguments
            scanBeforeCheckIn = oldSettings.scanBeforeCheckIn
        }
    }

    override fun getData() = MypyExecutorConfiguration(
        executablePath,
        useProjectSdk,
        configFilePath,
        arguments,
        projectDirectory,
        excludeNonProjectFiles,
        scanBeforeCheckIn
    )

    @TestOnly
    override fun reset() {
        loadState(MypyState())
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypySettings = project.service()
    }
}
