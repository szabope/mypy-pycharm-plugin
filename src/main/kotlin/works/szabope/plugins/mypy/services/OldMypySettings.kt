package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly

@Service(Service.Level.PROJECT)
@State(
    name = "MypyConfigService",
    storages = [Storage("mypy.xml", deprecated = true)],
    category = SettingsCategory.PLUGINS,
    allowLoadInTests = true,
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

    @TestOnly
    fun reset() {
        loadState(OldMypySettingsState())
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): OldMypySettings = project.service()
    }
}