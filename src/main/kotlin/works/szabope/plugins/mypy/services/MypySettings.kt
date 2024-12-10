package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.text.SemVer
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.MypyArgs
import works.szabope.plugins.mypy.dialog.MypyExecutionErrorDialog
import works.szabope.plugins.mypy.services.cli.Cli
import works.szabope.plugins.mypy.services.cli.PythonEnvironmentAwareCli
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import java.io.File
import javax.swing.event.HyperlinkEvent

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
            val validityProblem = validateExecutable(value)
            if (validityProblem == null) {
                state.mypyExecutable = value
            } else {
                logger.warn("mypyExecutable validation failed for '$validityProblem' for '$value'")
            }
        }

    var configFilePath
        get() = state.configFilePath
        set(value) {
            val validityProblem = validateConfigFile(value)
            if (validityProblem == null) {
                state.configFilePath = value
            } else {
                logger.warn("configFilePath validation failed with '$validityProblem' for '$value")
            }
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
            val validityProblem = validateProjectDirectory(value)
            if (validityProblem == null) {
                state.projectDirectory = value
            } else {
                logger.warn("projectDirectory validation failed for '$validityProblem' with '$value'")
            }
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

    class SettingsValidationProblem(val message: String) {
        override fun toString() = message
    }

    fun isComplete(): Boolean = state.mypyExecutable != null && projectDirectory != null

    fun ensureValid(): SettingsValidationProblem? {
        validateExecutable(state.mypyExecutable)?.also {
            logger.warn("clearing invalid mypyExecutable $mypyExecutable")
            mypyExecutable = null
            return@ensureValid it
        }
        validateConfigFile(state.configFilePath)?.also {
            logger.warn("clearing invalid configFilePath $configFilePath")
            configFilePath = null
            return@ensureValid it
        }
        validateProjectDirectory(state.projectDirectory)?.also {
            logger.warn("clearing invalid projectDirectory $projectDirectory")
            projectDirectory = null
            return@ensureValid it
        }
        return null
    }

    suspend fun initSettings(defaultExecutable: String?, defaultConfigFile: String?, defaultArguments: String?) {
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

    fun validateExecutable(path: String?): SettingsValidationProblem? {
        if (path != null) {
            require(path.isNotBlank())
            val file = File(path)
            if (!file.exists()) {
                return SettingsValidationProblem(MyBundle.message("mypy.settings.path_to_executable.not_exists"))
            }
            if (file.isDirectory) {
                return SettingsValidationProblem(MyBundle.message("mypy.settings.path_to_executable.is_directory"))
            }
            if (!file.canExecute()) {
                return SettingsValidationProblem(MyBundle.message("mypy.settings.path_to_executable.not_executable"))
            }

            val stdout = StringBuilder()
            val processResult = runBlocking {
                Cli().execute("$path -V") { it.collect(stdout::appendLine) }
            }
            if (processResult.resultCode != 0) {
                return SettingsValidationProblem(
                    MyBundle.message(
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
                return SettingsValidationProblem(MyBundle.message("mypy.settings.path_to_executable.unknown_version"))
            }
            if (!mypyVersion.isGreaterOrEqualThan(minimumMypyVersion)) {
                return SettingsValidationProblem(
                    MyBundle.message(
                        "mypy.settings.mypy_invalid_version", stdout.toString(), minimumMypyVersionText
                    )
                )
            }
        }
        return null
    }

    fun validateConfigFile(path: String?): SettingsValidationProblem? {
        if (path != null) {
            require(path.isNotBlank())
            val file = File(path)
            if (!file.exists()) {
                return SettingsValidationProblem(MyBundle.message("mypy.settings.path_to_config_file.not_exists"))
            }
            if (file.isDirectory) {
                return SettingsValidationProblem(MyBundle.message("mypy.settings.path_to_config_file.is_directory"))
            }
        }
        return null
    }

    fun validateProjectDirectory(path: String?): SettingsValidationProblem? {
        if (path != null) {
            require(path.isNotBlank())
            val file = File(path)
            if (!file.exists()) {
                return SettingsValidationProblem(MyBundle.message("mypy.settings.path_to_project_directory.not_exist"))
            }
            if (!file.isDirectory) {
                return SettingsValidationProblem(MyBundle.message("mypy.settings.path_to_project_directory.is_not_directory"))
            }
        }
        return null
    }

    private suspend fun autodetectExecutable(project: Project): String? {
        val locateCommand = if (SystemInfo.isWindows) "where.exe mypy.exe" else "which mypy"
        val stdout = StringBuilder()
        val processResult = PythonEnvironmentAwareCli(project).execute(locateCommand) { it.collect(stdout::appendLine) }
        return when (processResult.resultCode) { // same for linux and windows
            0 -> stdout.toString().lines().first().trim().ifBlank { null }
            1 -> null
            else -> {
                ToolWindowManager.getInstance(project).notifyByBalloon(
                    MypyToolWindowPanel.ID, MessageType.ERROR, MyBundle.message("mypy.toolwindow.balloon.error"), null
                ) {
                    if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                        MypyExecutionErrorDialog(locateCommand, processResult.stderr, processResult.resultCode).show()
                    }
                }
                null
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypySettings = project.service()

        val logger = thisLogger()
    }
}
