package works.szabope.plugins.mypy.activity

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.EntityChange
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.mypy.services.MypyIncompleteConfigurationNotificationService
import works.szabope.plugins.mypy.services.MypyPackageUtil
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings

internal class MypySettingsInitializationActivity : ProjectActivity {

    @TestOnly
    val configurationCalled = Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)

    override suspend fun execute(project: Project) {
        configureMypy(project)
        project.workspaceModel.eventLog.filter {
            it.getChanges(ModuleEntity::class.java).filterIsInstance<EntityChange.Replaced<ModuleEntity>>().isNotEmpty()
        }.collectLatest {
            configureMypy(project)
        }
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
        configurationCalled.send(Unit)
    }
}
