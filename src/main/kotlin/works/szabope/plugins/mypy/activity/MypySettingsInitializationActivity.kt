package works.szabope.plugins.mypy.activity

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.EntityChange
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.mypy.services.MypyIncompleteConfigurationNotificationService
import works.szabope.plugins.mypy.services.MypyPackageUtil
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings

internal class MypySettingsInitializationActivity : ProjectActivity {

    private var configurationDoneCallback: () -> Unit = {}

    override suspend fun execute(project: Project) {
        configureMypy(project)
        project.workspaceModel.eventLog.collectLatest {
            try {
                val changes = it.getChanges(ModuleEntity::class.java)
                if (changes.filterIsInstance<EntityChange.Replaced<ModuleEntity>>().isNotEmpty()) {
                    configureMypy(project)
                }
            } catch (e: CancellationException) { //TODO: remove?
                thisLogger().error(e)
                throw e
            }
        }
    }

    @TestOnly
    fun onConfigurationDone(callback: () -> Unit) {
        configurationDoneCallback = callback
    }

    private suspend fun configureMypy(project: Project) {
        val settings = MypySettings.getInstance(project)
        if (!settings.isComplete()) {
            with(OldMypySettings.getInstance(project)) {
                settings.initSettings(customMypyPath, mypyConfigFilePath, mypyArguments)
            }
        }
        if (!settings.isComplete()) {
            val notificationService = MypyIncompleteConfigurationNotificationService.getInstance(project)
            val canInstall = MypyPackageUtil.canInstall(project)
            notificationService.notify(canInstall)
        }
        configurationDoneCallback.invoke()
    }
}
