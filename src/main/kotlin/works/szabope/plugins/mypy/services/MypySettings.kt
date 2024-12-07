package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.text.SemVer
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.MypyArgs
import works.szabope.plugins.mypy.services.cli.Cli
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import java.io.File

@Service(Service.Level.PROJECT)
@State(name = "MypySettings", storages = [Storage("MypyPlugin.xml")], category = SettingsCategory.PLUGINS)
class MypySettings(internal val project: Project) :
    SimplePersistentStateComponent<MypySettings.MypyState>(MypyState()) {

    @ApiStatus.Internal
    class MypyState : BaseState() {
        var mypyExecutable by string()
        var configFilePath by string()
        var arguments by string()
        var autoScrollToSource by property(false)
        var excludeNonProjectFiles by property(true)
        var projectDirectory by string()
        val customExclusions by list<String>()
    }

    var mypyExecutable
        get() = state.mypyExecutable
        set(value) {
            validateExecutable(value)
            state.mypyExecutable = value
        }

    var configFilePath
        get() = state.configFilePath
        set(value) {
            state.configFilePath = value
        }

    var arguments
        get() = state.arguments
        set(value) {
            state.arguments = value
        }

    var isAutoScrollToSource
        get() = state.autoScrollToSource
        set(value) {
            state.autoScrollToSource = value
        }

    var isExcludeNonProjectFiles
        get() = state.excludeNonProjectFiles
        set(value) {
            state.excludeNonProjectFiles = value
        }

    var projectDirectory
        get() = state.projectDirectory
        set(value) {
            state.projectDirectory = value
        }

    val customExclusions
        get() = state.customExclusions

    fun addExclusion(exclusion: String) {
        require(exclusion.isNotBlank())
        if (!state.customExclusions.contains(exclusion)) {
            state.customExclusions.add(exclusion)
        }
    }

    fun removeExclusion(exclusion: String) {
        if (state.customExclusions.contains(exclusion)) {
            state.customExclusions.remove(exclusion)
        }
    }

    fun isInitialized(): Boolean = state.mypyExecutable != null && projectDirectory != null

    @Throws(SettingsValidationException::class)
    fun ensureValid() {
        try {
            if (state.mypyExecutable != null) {
                validateExecutable(state.mypyExecutable)
            }
        } catch (e: SettingsValidationException) {
            mypyExecutable = null
            throw e
        }
        try {
            if (state.configFilePath != null) {
                validateConfigFile(state.configFilePath)
            }
        } catch (e: SettingsValidationException) {
            configFilePath = null
            throw e
        }
        try {
            if (state.projectDirectory != null) {
                validateProjectDirectory(state.projectDirectory)
            }
        } catch (e: SettingsValidationException) {
            projectDirectory = null
            throw e
        }
    }

    class SettingsValidationException(val blame: String, message: String) : Exception(message)

    suspend fun initSettings(defaultExecutable: String?, defaultConfigFile: String?, defaultArguments: String?) {
        if (ProjectRootManager.getInstance(project).projectSdk == null) {
            return
        }
        if (mypyExecutable == null) {
            mypyExecutable = defaultExecutable ?: autodetectExecutable(project)
        }
        if (configFilePath == null) {
            configFilePath = defaultConfigFile
        }
        if (arguments == null) {
            arguments = defaultArguments ?: MypyArgs.MYPY_RECOMMENDED_COMMAND_ARGS
        }
        if (projectDirectory == null) {
            projectDirectory = project.guessProjectDir()?.path
        }
    }

    @Throws(SettingsValidationException::class)
    fun validateExecutable(path: String?) {
        if (path == null) {
            return
        }
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            throw SettingsValidationException(path, MyBundle.message("mypy.settings.path_to_executable.not_exists"))
        }
        if (file.isDirectory) {
            throw SettingsValidationException(path, MyBundle.message("mypy.settings.path_to_executable.is_directory"))
        }
        if (!file.canExecute()) {
            throw SettingsValidationException(
                path, MyBundle.message("mypy.settings.path_to_executable.not_executable")
            )
        }

        val stdout = StringBuilder()
        val processResult = runBlocking {
            Cli().execute("$path -V") { it.collect(stdout::appendLine) }
        }
        if (processResult.resultCode != 0) {
            throw SettingsValidationException(
                path, MyBundle.message(
                    "mypy.settings.path_to_executable.exited_with_error",
                    path,
                    processResult.resultCode,
                    processResult.stderr
                )
            )
        }
        val minimumMypyVersionText = MyBundle.message("mypy.minimumVersion")
        val minimumMypyVersion = SemVer.parseFromText(minimumMypyVersionText)!!
        val mypyVersion = "(\\d+.\\d+.\\d+)".toRegex().find(stdout)?.let { SemVer.parseFromText(it.value) }
        if (mypyVersion == null) {
            throw SettingsValidationException(
                path, MyBundle.message("mypy.settings.path_to_executable.unknown_version")
            )
        }
        if (!mypyVersion.isGreaterOrEqualThan(minimumMypyVersion)) {
            throw SettingsValidationException(
                path, MyBundle.message("mypy.settings.mypy_invalid_version", stdout.toString(), minimumMypyVersionText)
            )
        }
    }

    @Throws(SettingsValidationException::class)
    fun validateConfigFile(path: String?) {
        if (path == null) {
            return
        }
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            throw SettingsValidationException(path, MyBundle.message("mypy.settings.path_to_config_file.not_exists"))
        }
        if (file.isDirectory) {
            throw SettingsValidationException(
                path,
                MyBundle.message("mypy.settings.path_to_config_file.is_directory")
            )
        }
    }

    @Throws(SettingsValidationException::class)
    fun validateProjectDirectory(path: String?) {
        if (path == null) {
            return
        }
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            throw SettingsValidationException(
                path,
                MyBundle.message("mypy.settings.path_to_project_directory.not_exist")
            )
        }
        if (!file.isDirectory) {
            throw SettingsValidationException(
                path,
                MyBundle.message("mypy.settings.path_to_project_directory.is_not_directory")
            )
        }
    }

    private suspend fun autodetectExecutable(project: Project): String? {
        val locateCommand = if (SystemInfo.isWindows) "where mypy.exe" else "which mypy"
        val stdout = StringBuilder()
        val processResult = Cli().execute(locateCommand) { it.collect(stdout::appendLine) }
        if (processResult.resultCode != 0) {
            ToolWindowManager.getInstance(project).notifyByBalloon(
                MypyToolWindowPanel.ID, MessageType.ERROR, MyBundle.message(
                    "mypy.settings.path_to_executable.exited_with_error",
                    locateCommand,
                    processResult.resultCode,
                    processResult.stderr
                )
            )
            return null
        }
        return stdout.toString().lines().first().trim().ifBlank { null }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypySettings = project.service()
    }
}
