package works.szabope.plugins.mypy

import works.szabope.plugins.mypy.testutil.getPylintConfigurationNotCompleteNotification

class PluginInitializationFromScratchTest : AbstractToolWindowTestCase() {

    fun `test plugin initialized from scratch (no python sdk) results in notification`() {
        val actions = with(project) { getPylintConfigurationNotCompleteNotification() }.actions
        assertEquals(
            MypyBundle.message("mypy.intention.complete_configuration.text"),
            actions.single().templatePresentation.text
        )
    }
}
