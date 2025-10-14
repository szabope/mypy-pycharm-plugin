package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.run.ToolExecutor

class MypyExecutor(project: Project) : ToolExecutor(project, "mypy")
