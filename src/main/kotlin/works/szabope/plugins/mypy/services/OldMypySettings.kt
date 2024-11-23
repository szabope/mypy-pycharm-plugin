package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

@Service(Service.Level.PROJECT)
@State(
    name = "OldMypySettings", storages = [Storage("mypy.xml", deprecated = true)], category = SettingsCategory.PLUGINS
)
class OldMypySettings : SimplePersistentStateComponent<OldMypySettings.OldMypySettingsState>(
    OldMypySettingsState()
) {

    @ApiStatus.Internal
    class OldMypySettingsState : BaseState() {
        var customMypyPath by string()
        var mypyConfigFilePath by string()
        var mypyArguments by string()
    }

    val customMypyPath
        get() = state.customMypyPath
    val mypyConfigFilePath
        get() = state.mypyConfigFilePath
    val mypyArguments
        get() = state.mypyArguments

    companion object {
        @JvmStatic
        fun getInstance(project: Project): OldMypySettings = project.service()
    }
}