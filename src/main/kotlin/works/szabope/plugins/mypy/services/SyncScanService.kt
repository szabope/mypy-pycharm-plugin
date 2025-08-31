package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import works.szabope.plugins.common.run.ProcessException
import works.szabope.plugins.common.run.execute
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.ScanService
import works.szabope.plugins.common.services.tool.ToolOutputHandler
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.run.MypyExecutionEnvironmentFactory
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException

@Service(Service.Level.PROJECT)
class SyncScanService(private val project: Project) : ScanService<MypyMessage> {

    override fun scan(
        targets: Collection<VirtualFile>,
        configuration: ImmutableSettingsData,
        resultHandler: ToolOutputHandler<MypyMessage>
    ) {
        val environment = MypyExecutionEnvironmentFactory(project).createEnvironment(configuration, targets)
        runBlockingCancellable { //TODO: can I collect the flow outside blocking block?
            execute(environment).onFailure {
                if (it is ProcessException) {
                    thisLogger().error(
                        MypyBundle.message(
                            "mypy.executable.error", configuration, it.exitCode, it.stdErr
                        ), it
                    )
                } else {
                    thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
                }
            }.onSuccess { raw ->
                MypyOutputParser.parse(raw).onEach { message ->
                    message.onFailure {
                        if (it is MypyParseException) {
                            thisLogger().warn(
                                MypyBundle.message("mypy.executable.parsing-result-failed", configuration), it
                            )
                        } else {
                            thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
                        }
                    }
                }.filter { it.isSuccess }.map { it.getOrThrow() }.let { resultHandler.handle(it) }
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): SyncScanService = project.service()
    }
}
