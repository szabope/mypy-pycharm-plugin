package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.platform.workspace.jps.entities.ExcludeUrlEntity
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import com.intellij.testFramework.workspaceModel.updateProjectModel
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.mypy.AbstractToolWindowTestCase
import works.szabope.plugins.mypy.action.ScanActionUtil.isReadyToScan
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.testutil.*
import java.net.URI
import java.nio.file.Paths
import javax.swing.event.HyperlinkEvent
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/action/scan_sdk")
class ScanSdkTest : AbstractToolWindowTestCase() {

    private val dialogManager = TestDialogManager()

    override fun getTestDataPath() = "src/test/testData/action/scan_sdk"

    override fun setUp() {
        mockkObject(DialogManager.Companion)
        every { DialogManager.dialogManager } answers { dialogManager }
        super.setUp()
    }

    fun testManualScan() = withMockSdk("${Paths.get(testDataPath).absolutePathString()}/MockSdk") {
        myFixture.copyDirectoryToProject("/", "/")
        installMypy(with(project) { getProjectContext() })
        setUpSettings()
        val workspaceModel = WorkspaceModel.getInstance(project)
        val excludedDir = workspaceModel.currentSnapshot.entities(ContentRootEntity::class.java).first().url.append(
            "/excluded_dir"
        )
        val excludedEntity = ExcludeUrlEntity(excludedDir, object : EntitySource {
            override val virtualFileUrl: VirtualFileUrl?
                get() = excludedDir
        })
        lateinit var exclusionWorkspaceEntity: WorkspaceEntity
        runWriteActionAndWait {
            workspaceModel.updateProjectModel { model ->
                exclusionWorkspaceEntity = model.addEntity(excludedEntity)
            }
        }

        toolWindowManager.onBalloon {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URI("http://localhost").toURL()
                )
            )
        }
        dialogManager.onAnyDialog {
            fail(it.toString())
        }
        val target = workspaceModel.currentSnapshot.entities(ContentRootEntity::class.java).first().url.virtualFile!!
        scan(with(project) { getContext { it.add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) } })
        PlatformTestUtil.waitWhileBusy { !isReadyToScan(project) }
        runBlocking {
            waitUntilAssertSucceeds {
                treeUtil.assertStructure("+Found 1 issue(s) in 1 file(s)\n")
            }.also {
                treeUtil.expandAll()
                treeUtil.assertStructure(
                    """|-Found 1 issue(s) in 1 file(s)
                   | -src/a.py
                   |  Bracketed expression "[...]" is not valid as a type [valid-type] (0:-1) Did you mean "List[...]"?
                   |""".trimMargin()
                )
            }
        }
        runWriteActionAndWait {
            workspaceModel.updateProjectModel { model ->
                model.removeEntity(
                    exclusionWorkspaceEntity
                )
            }
        }
    }

    private fun setUpSettings() {
        with(MypySettings.getInstance(project)) {
            executablePath = null
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            useProjectSdk = true
            configFilePath = null
            scanBeforeCheckIn = false
            arguments = null
            excludeNonProjectFiles = true
        }
    }
}
