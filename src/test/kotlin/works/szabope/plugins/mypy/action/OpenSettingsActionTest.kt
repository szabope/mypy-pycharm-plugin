package works.szabope.plugins.mypy.action

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.testFramework.PlatformTestUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import works.szabope.plugins.common.run.ProcessException
import works.szabope.plugins.common.run.execute
import works.szabope.plugins.mypy.AbstractMypyTestCase
import works.szabope.plugins.mypy.services.MypySettings

class OpenSettingsActionTest : AbstractMypyTestCase() { //AbstractMypyHeavyPlatformTestCase() {

    override fun setUp() {
        super.setUp()
        mockkStatic(ShowSettingsUtil::class)
        val showSettingsUtil = mockk<ShowSettingsUtil>()
        every { ShowSettingsUtil.getInstance() } returns showSettingsUtil
    }

    fun `test open settings with empty executable path and mypy available`() {
        with(MypySettings.getInstance(project)) {
            executablePath = null
        }
        mockkStatic(::execute)
        every { execute(any(ExecutionEnvironment::class)) } answers {
            flowOf("/some/dummy/path")
        }
        PlatformTestUtil.invokeNamedAction(OpenSettingsAction.ID)
        PlatformTestUtil.waitWhileBusy {
            MypySettings.getInstance(project).executablePath == null
        }
        assertEquals("/some/dummy/path", MypySettings.getInstance(project).executablePath)
    }

    fun `test open settings with empty executable path and mypy not available`() {
        with(MypySettings.getInstance(project)) {
            executablePath = null
        }
        mockkStatic(::execute)
        every { execute(any(ExecutionEnvironment::class)) } returns callbackFlow {
            close(ProcessException(1, ""))
        }
        PlatformTestUtil.invokeNamedAction(OpenSettingsAction.ID)
        assertNull(MypySettings.getInstance(project).executablePath)
    }

    fun `test open settings with filled executable paths`() {
        with(MypySettings.getInstance(project)) {
            executablePath = "/some/dummy/path"
        }
        PlatformTestUtil.invokeNamedAction(OpenSettingsAction.ID)
        assertEquals("/some/dummy/path", MypySettings.getInstance(project).executablePath)
    }
}