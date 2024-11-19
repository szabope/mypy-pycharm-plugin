package works.szabope.plugins.mypy.activity

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings

internal class MypySettingsInitializationActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        with(OldMypySettings.getInstance(project)) {
            MypySettings.getInstance(project).initSettings(customMypyPath, mypyConfigFilePath, mypyArguments)
        }
    }
}