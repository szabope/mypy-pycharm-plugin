package works.szabope.plugins.mypy

import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.SdkDependency
import com.intellij.platform.workspace.jps.entities.SdkId
import com.intellij.platform.workspace.jps.entities.modifyModuleEntity
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.workspaceModel.updateProjectModel
import com.intellij.workspaceModel.ide.legacyBridge.findModuleEntity
import com.jetbrains.python.PyNames
import com.jetbrains.python.sdk.PythonSdkAdditionalData
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.python.sdk.switchToSdk
import io.mockk.*
import org.jdom.Element
import works.szabope.plugins.mypy.services.MypyPackageUtil
import works.szabope.plugins.mypy.testutil.PackageManagementTestService
import works.szabope.plugins.mypy.testutil.PyRemoteSdkAdditionalDataMock

/**
 * <pre>
 * So this class is pretty ugly. If you have a better solution (especially for replacing SDK), please,
 * send a PR or open an issue. Thanks.
 * When mypy configuration is not complete (ready to run mypy), a notification is shown to 'Complete configuration'
 * or - if there is a local SDK set - to install mypy if missing.
 * This notification is updated with changing SDK.
 * </pre>
 */
@TestDataPath("\$CONTENT_ROOT/testData/incompleteConfigurationNotification")
class MypyIncompleteConfigurationNotificationTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/incompleteConfigurationNotification"

    fun testNoSdkNotification() {
        val openSettingsAction = mockOpenSettingsAction()
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
        unmockkAll()
    }

    fun testLocalSdkNotification() = withLocalInterpreter {
        val packageManager = PackageManagementTestService()
        mockkObject(MypyPackageUtil)
        every { MypyPackageUtil.getPackageManager(project) } returns packageManager
        every { MypyPackageUtil.canInstall(project) } returns true

        val notification = getSettingsNotification()
        val actions = notification.actions
        assertEquals(2, actions.size)
        val action = AnActionWrapper(actions.last()) // Install mypy action
        val event = getAnActionEvent(notification)
        action.update(event)
        assertTrue(event.presentation.isEnabled)
        action.actionPerformed(event)
        assertNotEmpty(packageManager.installedPackagesList.filter { it.name == "mypy" })
        unmockkAll()
    }

    fun testRemoteSdkNotification() = withRemoteInterpreter {
        val openSettingsAction = mockOpenSettingsAction()
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
        unmockkAll()
    }

    private fun mockOpenSettingsAction(): AnAction {
        val openSettingsAction = ActionManager.getInstance().getAction("MyPyOpenSettingsAction")
        mockkObject(openSettingsAction)
        every { openSettingsAction.actionPerformed(any()) } returns Unit
        return openSettingsAction
    }

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

    private fun withLocalInterpreter(f: () -> Unit) {
        val additionalData = object : PythonSdkAdditionalData() {}
        withInterpreter(mockSdkType(true, additionalData), additionalData, f)
    }


    private fun withRemoteInterpreter(f: () -> Unit) {
        val additionalData = PyRemoteSdkAdditionalDataMock()
        val sdkType = mockSdkType(false, additionalData)
        withInterpreter(sdkType, additionalData, f)
    }

    private fun mockSdkType(localSdk: Boolean, additionalData: PythonSdkAdditionalData): PythonSdkType {
        val ctr = PythonSdkType::class.java.getDeclaredConstructor()
        ctr.isAccessible = true
        val pythonSdkType = ctr.newInstance()
        return spyk(pythonSdkType) {
            every { name } returns PyNames.PYTHON_SDK_ID_NAME
            every { isLocalSdk(any(Sdk::class)) } returns localSdk
            every { isValidSdkHome(any(String::class)) } returns true
            every { loadAdditionalData(any(Sdk::class), any(Element::class)) } returns additionalData
        }
    }

    private fun withInterpreter(sdkType: PythonSdkType, additionalData: PythonSdkAdditionalData, f: () -> Unit) {
        SdkType.EP_NAME.point.unregisterExtension(PythonSdkType::class.java)
        SdkType.EP_NAME.point.registerExtension(sdkType, testRootDisposable)
        val jdkTable = ProjectJdkTable.getInstance()
        val sdk = jdkTable.createSdk(PyNames.PYTHON_SDK_ID_NAME, sdkType)
        val modificator = sdk.sdkModificator
        modificator.homePath = "$testDataPath/MockSdk/bin/python"
        modificator.sdkAdditionalData = additionalData
        runWriteActionAndWait {
            modificator.commitChanges()
            jdkTable.addJdk(sdk)
        }
        switchToSdk(module, sdk, project.pythonSdk)
        awaitProcessed {
            runWriteActionAndWait {
                project.workspaceModel.updateProjectModel {
                    val moduleEntity = module.findModuleEntity(it)!!
                    it.modifyModuleEntity(moduleEntity) {
                        this.dependencies.removeIf { dep -> dep is SdkDependency }
                        this.dependencies.add(SdkDependency(SdkId(PyNames.PYTHON_SDK_ID_NAME, sdkType.name)))
                    }
                }
            }
        }
        f.invoke()
        runWriteActionAndWait {
            jdkTable.removeJdk(sdk)
        }
    }
}