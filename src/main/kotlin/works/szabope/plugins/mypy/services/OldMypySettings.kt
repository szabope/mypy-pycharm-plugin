package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.common.services.BasicSettingsData

@Service(Service.Level.PROJECT)
@State(
    name = "MypyConfigService",
    storages = [Storage("mypy.xml", deprecated = true)],
    category = SettingsCategory.PLUGINS,
    allowLoadInTests = true,
)
class OldMypySettings : SimplePersistentStateComponent<OldMypySettings.OldMypySettingsState>(
    OldMypySettingsState()
), BasicSettingsData {

    @ApiStatus.Internal
    class OldMypySettingsState : BaseState() {
        var customMypyPath by string()
        var mypyConfigFilePath by string()
        var mypyArguments by string()
    }

    override val executablePath get() = state.customMypyPath
    override val configFilePath get() = state.mypyConfigFilePath
    override val arguments get() = state.mypyArguments
    override val scanBeforeCheckIn get() = false

    companion object {
        @JvmStatic
        fun getInstance(project: Project): OldMypySettings = project.service()
    }
}