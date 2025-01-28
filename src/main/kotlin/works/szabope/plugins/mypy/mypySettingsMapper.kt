package works.szabope.plugins.mypy

import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings

fun MypySettings.toRunConfiguration() = MypyService.RunConfiguration(
    configFilePath, arguments, isExcludeNonProjectFiles, customExclusions, projectDirectory!!
)
