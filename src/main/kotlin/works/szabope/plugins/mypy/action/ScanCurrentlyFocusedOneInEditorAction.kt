package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile

class ScanCurrentlyFocusedOneInEditorAction : ScanAction() {
    override fun listTargets(event: AnActionEvent): List<VirtualFile>? {
        val project = event.project ?: return null
        return FileEditorManager.getInstance(project).selectedTextEditor?.virtualFile?.let { listOf(it) }
    }
}