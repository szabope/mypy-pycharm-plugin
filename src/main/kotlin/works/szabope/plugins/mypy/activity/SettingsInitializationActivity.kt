package works.szabope.plugins.mypy.activity

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.annotations.TestOnly
import works.szabope.plugins.mypy.services.MypyIncompleteConfigurationNotificationService
import works.szabope.plugins.mypy.services.MypyPackageUtil
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings

internal class SettingsInitializationActivity : ProjectActivity {

    @TestOnly
    val configurationCalled = Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)

    override suspend fun execute(project: Project) {
        configurePlugin(project)
        if (!ApplicationManager.getApplication().isUnitTestMode) {
            project.workspaceModel.eventLog.filter {
                it.getChanges(ModuleEntity::class.java).isNotEmpty()
            }.collectLatest {
                configurePlugin(project)
            }
        }
    }

    @TestOnly
    suspend fun configurePlugin(project: Project) {
        MypyPackageUtil.reloadPackages(project)
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
