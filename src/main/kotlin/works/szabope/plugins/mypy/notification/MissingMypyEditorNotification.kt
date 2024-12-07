package works.szabope.plugins.mypy.notification

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyPackageManagerService
import works.szabope.plugins.mypy.services.MypyService.Companion.SUPPORTED_FILE_TYPES
import works.szabope.plugins.mypy.services.MypySettings
import java.util.function.Function
import javax.swing.JComponent

class MissingMypyEditorNotificationPanel(fileEditor: FileEditor, canMypyBeInstalled: Boolean) :
    EditorNotificationPanel(fileEditor, Status.Warning) {

    init {
        createActionLabel(
            MyBundle.message("mypy.intention.complete_configuration.text"), "MyPyOpenSettingsAction"
        )
        if (canMypyBeInstalled) {
            createActionLabel(MyBundle.message("mypy.intention.install_mypy.text"), "InstallMypyAction")
        }
        text(MyBundle.message("mypy.settings.incomplete"))
    }

    override fun getIntentionActionFamilyName(): String {
        return MyBundle.message("mypy.intention.configuration.family_name")
    }
}

internal class MypyEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(
        project: Project, file: VirtualFile
    ): Function<in FileEditor, out JComponent?> {
        return Function {
            val sdk = ProjectRootManager.getInstance(project).projectSdk
            if (sdk != null && !isSettingsInitialized(project) && file.fileType in SUPPORTED_FILE_TYPES) {
                return@Function MissingMypyEditorNotificationPanel(it, canMypyBeInstalled(project, sdk))
            }
            return@Function null
        }
    }

    private fun canMypyBeInstalled(project: Project, sdk: Sdk) =
        sdk.sdkType.isLocalSdk(sdk) && !MypyPackageManagerService.getInstance(project).isInstalled(sdk)

    private fun isSettingsInitialized(project: Project) = MypySettings.getInstance(project).isInitialized()
}
