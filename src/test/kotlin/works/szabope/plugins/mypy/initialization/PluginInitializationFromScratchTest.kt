package works.szabope.plugins.mypy.initialization

import works.szabope.plugins.mypy.AbstractToolWindowTestCase
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.testutil.getMypyConfigurationNotCompleteNotification

class PluginInitializationFromScratchTest : AbstractToolWindowTestCase() {

    fun `test plugin initialized from scratch (no python sdk) results in notification`() {
        val actions = getMypyConfigurationNotCompleteNotification(project).actions
        assertEquals(
            MypyBundle.message("mypy.intention.complete_configuration.text"),
            actions.single().templatePresentation.text
        )
    }
}