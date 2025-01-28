package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.util.text.SemVer
import com.jetbrains.python.sdk.pythonSdk
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.MypyArgs
import works.szabope.plugins.mypy.services.cli.Cli
import java.io.File

@Service(Service.Level.PROJECT)
@State(name = "MypySettings", storages = [Storage("MypyPlugin.xml")], category = SettingsCategory.PLUGINS)
class MypySettings(internal val project: Project) :
    SimplePersistentStateComponent<MypySettings.MypyState>(MypyState()) {

    @ApiStatus.Internal
    class MypyState : BaseState() {
        var configFilePath by string()
        var arguments by string()
        var autoScrollToSource by property(false)
        var excludeNonProjectFiles by property(true)
        var projectDirectory by string()
        val customExclusions by list<String>()
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

    @JvmInline
    value class SettingsValidationProblem(val message: String) {
        override fun toString() = message
    }

    fun isComplete(): Boolean = projectDirectory != null

    fun ensureValid(): SettingsValidationProblem? {
        validateExecutable()?.also {
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

    suspend fun initSettings(defaultConfigFile: String?, defaultArguments: String?) {
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

    fun validateExecutable(): SettingsValidationProblem? {
        if (true) {
            val stdout = StringBuilder()
            val processResult = runBlocking {
                Cli().execute(listOf(project.pythonSdk?.homePath!!, "-m", "mypy", "-V")) { it.collect(stdout::appendLine) }
            }
            if (processResult.resultCode != 0) {
                return SettingsValidationProblem(
                    MyBundle.message(
                        "mypy.settings.path_to_executable.exited_with_error",
                        "${project.pythonSdk?.homePath} -m mypy",
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

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypySettings = project.service()

        val logger = thisLogger()
    }
}
