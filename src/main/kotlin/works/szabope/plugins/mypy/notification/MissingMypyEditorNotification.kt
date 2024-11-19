package works.szabope.plugins.mypy.notification

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.jetbrains.python.pyi.PyiFileType
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyInstallerService
import works.szabope.plugins.mypy.services.MypyService.Companion.SUPPORTED_FILE_TYPES
import works.szabope.plugins.mypy.services.MypySettings
import java.util.function.Function
import javax.swing.JComponent

class MissingMypyEditorNotificationPanel(fileEditor: FileEditor, isMypyInstalled: Boolean) :
    EditorNotificationPanel(fileEditor, Status.Warning) {

    init {
        createActionLabel(
            MyBundle.message("mypy.intention.fix_executable.open_settings.text"), "MypyOpenSettingsAction"
        )
        if (!isMypyInstalled) {
            @Suppress("DialogTitleCapitalization") createActionLabel(
                MyBundle.message("mypy.intention.fix_executable.install_mypy.text"), "InstallMypyAction"
            )
        }
        text(MyBundle.message("mypy.settings.path_to_executable.missing"))
    }

    override fun getIntentionActionFamilyName(): String {
        return MyBundle.message("mypy.intention.fix_executable.family_name")
    }
}

internal class MypyEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(
        project: Project, file: VirtualFile
    ): Function<in FileEditor, out JComponent?> {
        PyiFileType.INSTANCE
        return Function {
            if (isSdkSet(project) && !isSettingsInitialized(project) && file.fileType in SUPPORTED_FILE_TYPES) {
                val isMypyInstalled = MypyInstallerService.getInstance(project).isMypyInstalled()
                return@Function MissingMypyEditorNotificationPanel(it, isMypyInstalled)
            }
            return@Function null
        }
    }

    private fun isSdkSet(project: Project): Boolean = ProjectRootManager.getInstance(project).projectSdk != null
    private fun isSettingsInitialized(project: Project): Boolean = MypySettings.getInstance(project).isInitialized()
}