package works.szabope.plugins.mypy.activity

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.EntityChange
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import works.szabope.plugins.mypy.services.MypyIncompleteConfigurationNotificationService
import works.szabope.plugins.mypy.services.MypyPackageManagerService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings

internal class MypySettingsInitializationActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        configureMypy(project)
        project.workspaceModel.eventLog.onEach {
            val moduleChanges = it.getChanges(ModuleEntity::class.java)
            if (moduleChanges.filterIsInstance<EntityChange.Replaced<ModuleEntity>>().isNotEmpty()) {
                configureMypy(project)
            }
        }.collect()
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
            val canInstall = MypyPackageManagerService.getInstance(project).canInstall()
            notificationService.notify(canInstall)
        }
    }

}
