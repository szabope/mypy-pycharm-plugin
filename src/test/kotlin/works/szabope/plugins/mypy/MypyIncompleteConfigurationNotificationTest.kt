package works.szabope.plugins.mypy

import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.project.modules
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.roots.ModuleRootManager
import com.jetbrains.python.PyNames
import com.jetbrains.python.sdk.PythonSdkAdditionalData
import com.jetbrains.python.sdk.pipenv.PyPipEnvSdkAdditionalData
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import works.szabope.plugins.mypy.services.MypyPackageUtil
import works.szabope.plugins.mypy.testutil.TestPackageManagementService
import works.szabope.plugins.mypy.testutil.TestPythonLocalSdkType

class MypyIncompleteConfigurationNotificationTest : AbstractToolWindowTestCase() {

    private lateinit var packageManager: TestPackageManagementService

    override fun setUp() {
        super.setUp()
        packageManager = TestPackageManagementService()
        mockkObject(MypyPackageUtil)
        every { MypyPackageUtil.getPackageManager(project) } returns packageManager
    }

    override fun tearDown() {
        unmockkAll()
        super.tearDown()
    }

    fun testNoSdkNotification() {
        val openSettingsAction = ActionManager.getInstance().getAction("MyPyOpenSettingsAction")
        mockkObject(openSettingsAction)
        every { openSettingsAction.actionPerformed(any()) } returns Unit
        val notification = getSettingsNotification()
        val actions = notification.actions
        assertEquals(1, actions.size)
        val action = AnActionWrapper(actions.first()) // Complete configuration action
        val event = getAnActionEvent(notification)
        action.update(event)
        assertTrue(event.presentation.isEnabled)
        action.actionPerformed(event)
        verify {
            openSettingsAction.actionPerformed(any(AnActionEvent::class))
        }
    }

    fun testLocalSdkNotification() {
        withInterpreter(TestPythonLocalSdkType()) {
            val notification = getSettingsNotification()
            val actions = notification.actions
            assertEquals(2, actions.size)
            val action = AnActionWrapper(actions.last()) // Install mypy action
            val event = getAnActionEvent(notification)
            action.update(event)
            assertTrue(event.presentation.isEnabled)
            action.actionPerformed(event)
            assertNotEmpty(packageManager.installedPackagesList.filter { it.name == "mypy" })
        }
    }

//    fun testRemoteSdkNotification() = runBlocking {
//    }

    private fun getAnActionEvent(notification: Notification): AnActionEvent {
        val context =
            SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).add(Notification.KEY, notification).build()
        return AnActionEvent.createEvent(context, null, ActionPlaces.NOTIFICATION, ActionUiKind.NONE, null)
    }

    private fun getSettingsNotification(): Notification {
        val notifications = ActionCenter.getNotifications(project).filter {
            "Mypy Group" == it.groupId && MyBundle.message("mypy.settings.incomplete") == it.content && !it.isExpired
        }
        assertEquals(1, notifications.size) // there can be only one... in general
        return notifications.first()
    }

    private inline fun <reified T : SdkType> withInterpreter(
        sdkType: T, additionalData: PythonSdkAdditionalData = PyPipEnvSdkAdditionalData(), f: () -> Unit
    ) {
        SdkType.EP_NAME.point.registerExtension(sdkType, testRootDisposable)
        val sdk = runWriteActionAndWait { ProjectJdkTable.getInstance().createSdk(PyNames.PYTHON_SDK_ID_NAME, sdkType) }
        val modificator = sdk.sdkModificator
        modificator.sdkAdditionalData = additionalData
        sdkType.setupSdkPaths(sdk)
        awaitProcessed {
            runWriteActionAndWait {
                modificator.commitChanges()
                ProjectJdkTable.getInstance().addJdk(sdk)
                with(ModuleRootManager.getInstance(project.modules[0]).modifiableModel) {
                    this.sdk = sdk
                    commit()
                }
            }
        }
        f.invoke()
        runWriteActionAndWait {
            with(ModuleRootManager.getInstance(project.modules[0]).modifiableModel) {
                this.sdk = null
                commit()
            }
            ProjectJdkTable.getInstance().removeJdk(sdk)
        }
    }

    private suspend fun setupRemoteInterpreter() {
        //TODO: after little cleanup
    }
}