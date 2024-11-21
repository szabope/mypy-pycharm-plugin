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
import works.szabope.plugins.mypy.activity.MypySettingsInitializationActivity
import works.szabope.plugins.mypy.services.cli.PyVirtualEnvCli
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
    }

    var mypyExecutable
        get() = state.mypyExecutable
        set(value) {
            validateMypyExecutable(value)
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

    fun isInitialized(): Boolean {
        return state.mypyExecutable != null
    }

    class MypyConfigurationValidationException(message: String) : Exception(message)

    suspend fun initSettings(defaultMypyPath: String?, defaultConfigFilePath: String?, defaultArguments: String?) {
        if (ProjectRootManager.getInstance(project).projectSdk == null) {
            return
        }
        if (mypyExecutable == null) {
            try {
                mypyExecutable = defaultMypyPath ?: autodetectMypyExecutable(project)
            } catch (e: MypyConfigurationValidationException) {
                logger<MypySettingsInitializationActivity>().info("Mypy not found")
            }
        }
        if (configFilePath == null) {
            configFilePath = defaultConfigFilePath
        }
        if (arguments == null) {
            arguments = defaultArguments ?: MypyArgs.MYPY_RECOMMENDED_COMMAND_ARGS
        }
    }

    @Throws(MypyConfigurationValidationException::class)
    fun validateMypyExecutable(path: String?) {
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
        runBlocking {
            PyVirtualEnvCli(project).execute("$path -V") { it.collect(stdout::appendLine) }
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

    @Throws(MypyConfigurationValidationException::class)
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

    private suspend fun autodetectMypyExecutable(project: Project): String? {
        val locateCommand = if (SystemInfo.isWindows) "where mypy.exe" else "which mypy"
        val stdout = StringBuilder()
        PyVirtualEnvCli(project).execute(locateCommand) { it.collect(stdout::appendLine) }
        return stdout.toString().lines().first().trim().ifBlank { null }
    }

    private fun fail(key: String, vararg args: String) {
        throw MypyConfigurationValidationException(MyBundle.message(key, args.asList()))
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypySettings = project.service()
    }
}
