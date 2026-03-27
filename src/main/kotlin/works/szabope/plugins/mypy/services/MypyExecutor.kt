package works.szabope.plugins.mypy.services

import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.run.ToolExecutor

class MypyExecutor(project: Project) : ToolExecutor(project, "mypy") {
    // exit code 1 should be fine https://github.com/python/mypy/issues/6003
    override fun isError(event: ProcessEvent): Boolean = event.exitCode > 2
}
