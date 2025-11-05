@file:Suppress("UnstableApiUsage")

package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil.performAction
import com.intellij.openapi.actionSystem.ex.ActionUtil.updateAction
import org.junit.Assert
import works.szabope.plugins.mypy.action.InstallMypyAction
import works.szabope.plugins.mypy.action.ScanAction
import works.szabope.plugins.mypy.action.StopScanAction

/**
 * Invoke named action with custom context
 * @see com.intellij.testFramework.PlatformTestUtil.invokeNamedAction
 */
fun scan(context: DataContext) {
    val action = ActionManager.getInstance().getAction(ScanAction.ID)
    val event = AnActionEvent.createEvent(context, null, "", ActionUiKind.NONE, null)
    PerformWithDocumentsCommitted.commitDocumentsIfNeeded(action, event)
    updateAction(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun stopScan(context: DataContext) {
    val action = ActionManager.getInstance().getAction(StopScanAction.ID)
    val event = AnActionEvent.createEvent(context, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    updateAction(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun installMypy(context: DataContext) {
    val action = ActionManager.getInstance().getAction(InstallMypyAction.ID)
    val event = AnActionEvent.createEvent(context, null, ActionPlaces.NOTIFICATION, ActionUiKind.NONE, null)
    PerformWithDocumentsCommitted.commitDocumentsIfNeeded(action, event)
    updateAction(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun markExcluded(context: DataContext) {
    if (context.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.isNotEmpty() != true) {
        throw IllegalArgumentException("Use `CommonDataKeys.VIRTUAL_FILE_ARRAY` for virtual files to exclude them")
    }
    val event = AnActionEvent.createEvent(context, null, "", ActionUiKind.NONE, null)
    val action = ActionManager.getInstance().getAction("MarkExcludeRoot")
    updateAction(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun unmark(context: DataContext) {
    val event = AnActionEvent.createEvent(context, null, "", ActionUiKind.NONE, null)
    if (event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).isNullOrEmpty()) {
        throw IllegalArgumentException("Use `CommonDataKeys.VIRTUAL_FILE_ARRAY` for virtual files to (un)mark them")
    }
    val action = ActionManager.getInstance().getAction("UnmarkRoot")
    updateAction(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}