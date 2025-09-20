package works.szabope.plugins.mypy

import works.szabope.plugins.mypy.testutil.getMypyConfigurationNotCompleteNotification

class PluginInitializationFromScratchTest : AbstractToolWindowTestCase() {

    fun `test plugin initialized from scratch (no python sdk) results in notification`() {
        val actions = with(project) { getMypyConfigurationNotCompleteNotification() }.actions
        assertEquals(
            MypyBundle.message("mypy.intention.complete_configuration.text"),
            actions.single().templatePresentation.text
        )
    }
}
