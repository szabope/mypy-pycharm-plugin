package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.text.SemVer
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.cli.PyVirtualEnvCli
import java.io.File

@Service(Service.Level.PROJECT)
@State(name = "MypySettings", storages = [Storage("MypyPlugin.xml")], category = SettingsCategory.PLUGINS)
class MypySettings(internal val project: Project) :
    SimplePersistentStateComponent<MypySettings.MypyState>(MypyState()) {
    private val logger = logger<MypySettings>()

    @ApiStatus.Internal
    class MypyState : BaseState() {
        var mypyExecutable by string()
        var configFilePath by string()
        var arguments by string()
        var autoScrollToSource by property(false)
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

    /**
     * @return true if configuration is initialized and valid (ready to be used), false otherwise
     */
    fun ensureValidOrUninitialized(): Boolean {
        try {
            if (state.mypyExecutable != null) {
                validateExecutable(state.mypyExecutable)
            }
        } catch (e: ConfigurationValidationException) {
            logger.error(MyBundle.message("mypy.settings.path_to_executable.invalid", state.mypyExecutable ?: ""), e)
            mypyExecutable = null
        }
        try {
            if (state.configFilePath != null) {
                validateConfigFile(state.configFilePath)
            }
        } catch (e: ConfigurationValidationException) {
            logger.error(MyBundle.message("mypy.settings.path_to_config_file.invalid", state.configFilePath ?: ""), e)
            configFilePath = null
        }
        return mypyExecutable != null
    }

    class ConfigurationValidationException(message: String) : Exception(message)

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
    }

    @Throws(ConfigurationValidationException::class)
    fun validateExecutable(path: String?) {
        if (path == null) {
            return
        }
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            fail("mypy.settings.path_to_executable.not_exists")
        }
        if (file.isDirectory) {
            fail("mypy.settings.path_to_executable.is_directory")
        }
        if (!file.canExecute()) {
            fail("mypy.settings.path_to_executable.not_executable")
        }

        val stdout = StringBuilder()
        val processResult = runBlocking {
            PyVirtualEnvCli(project).execute("$path -V") { it.collect(stdout::appendLine) }
        }
        if (processResult.resultCode != 0) {
            fail(
                "mypy.settings.path_to_executable.exited_with_error",
                path,
                processResult.resultCode,
                processResult.stderr
            )
        }
        val minimumMypyVersionText = MyBundle.message("mypy.minimumVersion")
        val minimumMypyVersion = SemVer.parseFromText(minimumMypyVersionText)!!
        val mypyVersion = "(\\d+.\\d+.\\d+)".toRegex().find(stdout)?.let { SemVer.parseFromText(it.value) }
        if (mypyVersion == null) {
            fail("mypy.settings.path_to_executable.unknown_version")
        }
        if (!mypyVersion!!.isGreaterOrEqualThan(minimumMypyVersion)) {
            fail("mypy.settings.mypy_invalid_version", stdout.toString(), minimumMypyVersionText)
        }
    }

    @Throws(ConfigurationValidationException::class)
    fun validateConfigFile(path: String?) {
        if (path == null) {
            return
        }
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            fail("mypy.settings.path_to_config_file.not_exists")
        }
        if (file.isDirectory) {
            fail("mypy.settings.path_to_config_file.is_directory")
        }
    }

    private suspend fun autodetectExecutable(project: Project): String? {
        val locateCommand = if (SystemInfo.isWindows) "where mypy.exe" else "which mypy"
        val stdout = StringBuilder()
        val processResult = PyVirtualEnvCli(project).execute(locateCommand) { it.collect(stdout::appendLine) }
        if (processResult.resultCode != 0) {
            logger.warn(
                ConfigurationValidationException(
                    MyBundle.message(
                        "mypy.settings.path_to_executable.exited_with_error",
                        locateCommand,
                        processResult.resultCode,
                        processResult.stderr
                    )
                )
            )
            return null
        }
        return stdout.toString().lines().first().trim().ifBlank { null }
    }

    private fun fail(key: String, vararg args: Any) {
        throw ConfigurationValidationException(MyBundle.message(key, args.asList()))
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypySettings = project.service()
    }
}
