package works.szabope.plugins.mypy.action

import com.intellij.openapi.options.BoundSearchableConfigurable
import works.szabope.plugins.common.action.AbstractOpenSettingsAction
import works.szabope.plugins.mypy.configurable.MypyConfigurable

class OpenSettingsAction : AbstractOpenSettingsAction() {
    override fun getConfigurableClass(): Class<out BoundSearchableConfigurable> = MypyConfigurable::class.java

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.OpenSettingsAction"
    }
}
