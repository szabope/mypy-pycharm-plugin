package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil.performAction
import org.junit.Assert
import works.szabope.plugins.common.test.action.updateActionForTest
import works.szabope.plugins.mypy.action.InstallMypyAction
import works.szabope.plugins.mypy.action.ScanAction
import works.szabope.plugins.mypy.action.StopScanAction

fun scan(context: DataContext) {
    val action = ActionManager.getInstance().getAction(ScanAction.ID)
    val event = AnActionEvent.createEvent(context, null, "", ActionUiKind.NONE, null)
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun stopScan(context: DataContext) {
    val action = ActionManager.getInstance().getAction(StopScanAction.ID)
    val event = AnActionEvent.createEvent(context, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun installMypy(context: DataContext) {
    val action = ActionManager.getInstance().getAction(InstallMypyAction.ID)
    val event = AnActionEvent.createEvent(context, null, ActionPlaces.NOTIFICATION, ActionUiKind.NONE, null)
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}
