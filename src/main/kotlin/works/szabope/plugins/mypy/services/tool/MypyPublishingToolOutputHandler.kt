package works.szabope.plugins.mypy.services.tool

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.tool.AbstractPublishingToolOutputHandler
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyMessageConverter
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

class MypyPublishingToolOutputHandler(private val project: Project) :
    AbstractPublishingToolOutputHandler<MypyMessage>(MypyMessageConverter) {
    override val treeService: ITreeService
        get() = MypyTreeService.getInstance(project)
}