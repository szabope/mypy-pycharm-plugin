package works.szabope.plugins.mypy.services

import com.intellij.icons.AllIcons
import works.szabope.plugins.common.services.SeverityConfig
import works.szabope.plugins.mypy.MypyBundle

val mypySeverityConfigs = mapOf(
    "ERROR" to SeverityConfig(
        "ERROR",
        MypyBundle.message("action.MyPyDisplayErrorsAction.text"),
        MypyBundle.message("action.MyPyDisplayErrorsAction.description"),
        AllIcons.General.Error
    ),

    "WARNING" to SeverityConfig(
        "WARNING",
        MypyBundle.message("action.MypyDisplayWarningsAction.text"),
        MypyBundle.message("action.MypyDisplayWarningsAction.description"),
        AllIcons.General.Warning
    ),

    "NOTE" to SeverityConfig(
        "NOTE",
        MypyBundle.message("action.MypyDisplayNoteAction.text"),
        MypyBundle.message("action.MypyDisplayNoteAction.description"),
        AllIcons.General.Information
    )
)
