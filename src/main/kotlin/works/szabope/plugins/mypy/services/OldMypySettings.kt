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

    override val executablePath: String?
        get() = state.customMypyPath
    override val configFilePath: String?
        get() = state.mypyConfigFilePath
    override val arguments: String?
        get() = state.mypyArguments
    override val scanBeforeCheckIn: Boolean
        get() = throw UnsupportedOperationException("Old Mypy plugin never supported this")

    companion object {
        @JvmStatic
        fun getInstance(project: Project): OldMypySettings = project.service()
    }
}