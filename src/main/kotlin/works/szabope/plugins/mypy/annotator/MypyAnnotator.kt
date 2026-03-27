package works.szabope.plugins.mypy.annotator

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.annotator.ToolAnnotator
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.SyncScanService
import works.szabope.plugins.mypy.services.parser.MypyMessage

class MypyAnnotator : ToolAnnotator<MypyMessage>() {
    override val inspectionId = MypyInspectionId

    override fun getSettings(project: Project) = MypySettings.getInstance(project)

    override fun scan(info: AnnotatorInfo, configuration: ToolExecutorConfiguration) =
        SyncScanService.getInstance(info.project).scan(listOf(info.file), configuration)[info.file] ?: emptyList()

    override fun createIntention(message: MypyMessage) = MypyIgnoreIntention(message)
}
