package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.blankToSingleSpace
import works.szabope.plugins.common.services.AbstractToolSettings

@Service(Service.Level.PROJECT)
@State(name = "MypySettings", storages = [Storage("MypyPlugin.xml")], category = SettingsCategory.PLUGINS)
class MypySettings(private val project: Project) : AbstractToolSettings<MypySettings.MypyState>(project, MypyState()) {

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
        set(value) { state.useProjectSdk = value }

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
        set(value) { state.autoScrollToSource = value }

    override var excludeNonProjectFiles
        get() = state.excludeNonProjectFiles
        set(value) { state.excludeNonProjectFiles = value }

    override var workingDirectory
        get() = state.projectDirectory
        set(value) { state.projectDirectory = value }

    override var scanBeforeCheckIn
        get() = state.scanBeforeCheckIn
        set(value) { state.scanBeforeCheckIn = value }

    override fun getPackageManagementService() = MypyPluginPackageManagementService.getInstance(project)
    override fun toolNotSetMessage() = "Mypy tool is not set"
    override fun isExecutableStateNull() = state.mypyExecutable == null
    override fun isConfigFileStateNull() = state.configFilePath == null
    override fun isArgumentsStateNull() = state.arguments == null
    override fun initialState() = MypyState()

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypySettings = project.service()
    }
}