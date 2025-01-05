package works.szabope.plugins.mypy.configurable

import com.intellij.grazie.utils.trimToNull
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.dsl.builder.*
import org.jetbrains.annotations.ApiStatus
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.MypyArgs
import works.szabope.plugins.mypy.services.MypySettings

internal class MypySettingConfigurable(private val project: Project) : BoundSearchableConfigurable(
    MyBundle.message("mypy.configurable.name"), MyBundle.message("mypy.configurable.name"), _id = ID
), Configurable.NoScroll {

    private val settings
        get() = MypySettings.getInstance(project)

    private val mypyExecutableChooserDescriptor =
        FileChooserDescriptor(true, false, false, false, false, false).withFileFilter(
            FileFilter(
                if (SystemInfo.isWindows) {
                    listOf("mypy.exe", "mypyc.exe", "mypy.bat")
                } else {
                    listOf("mypy", "mypyc")
                }
            )
        )

    private val directoryChooserDescriptor = FileChooserDescriptor(false, true, false, false, false, false)

    override fun createPanel(): DialogPanel {
        return panel {
            indent {
                row {
                    label(MyBundle.message("mypy.settings.path_to_executable.label"))
                    textFieldWithBrowseButton(
                        project = project, fileChooserDescriptor = mypyExecutableChooserDescriptor
                    ).align(Align.FILL).bindText(
                        getter = { settings.mypyExecutable.orEmpty() },
                        setter = { settings.mypyExecutable = it.trimToNull() },
                    ).validationOnInput { field ->
                        if (field.text.isBlank()) {
                            val message = MyBundle.message("mypy.settings.path_to_executable.empty_warning")
                            return@validationOnInput warning(message)
                        }
                        settings.validateExecutable(field.text.trimToNull())?.also {
                            return@validationOnInput error(it.message)
                        }
                        null
                    }
                }.rowComment(
                    MyBundle.message(
                        "mypy.settings.path_to_executable.comment", MypyArgs.MYPY_MANDATORY_COMMAND_ARGS
                    ), maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
                ).layout(RowLayout.PARENT_GRID)
                row {
                    label(MyBundle.message("mypy.settings.config_file.label"))
                    textFieldWithBrowseButton(project = project).align(Align.FILL).bindText(
                        getter = { settings.configFilePath.orEmpty() },
                        setter = { settings.configFilePath = it.trimToNull() },
                    ).validationOnInput { field ->
                        settings.validateConfigFile(field.text.trimToNull())?.also {
                            return@validationOnInput error(it.message)
                        }
                        null
                    }
                }.rowComment(
                    MyBundle.message("mypy.settings.config_file.comment"), maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
                ).layout(RowLayout.PARENT_GRID)
                row {
                    label(MyBundle.message("mypy.settings.arguments.label"))
                    textField().align(Align.FILL).bindText(
                        getter = { settings.arguments.orEmpty() },
                        setter = { settings.arguments = it.trimToNull() },
                    )
                }.rowComment(
                    MyBundle.message(
                        "mypy.settings.arguments.hint_recommended", MypyArgs.MYPY_RECOMMENDED_COMMAND_ARGS
                    ), maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
                ).layout(RowLayout.PARENT_GRID)
                row {
                    label(MyBundle.message("mypy.settings.project_directory.label"))
                    textFieldWithBrowseButton(
                        project = project, fileChooserDescriptor = directoryChooserDescriptor
                    ).align(Align.FILL).bindText(
                        getter = { settings.projectDirectory.orEmpty() },
                        setter = { settings.projectDirectory = it.trimToNull() },
                    ).validationOnInput { field ->
                        if (field.text.isBlank()) {
                            val message = MyBundle.message("mypy.settings.path_to_project_directory.empty_warning")
                            return@validationOnInput warning(message)
                        }
                        settings.validateProjectDirectory(field.text.trimToNull())?.also {
                            return@validationOnInput error(it.message)
                        }
                        null
                    }
                }
                row {
                    checkBox("Exclude non-project files").bindSelected(
                        getter = { settings.isExcludeNonProjectFiles },
                        setter = { settings.isExcludeNonProjectFiles = it })
                }.layout(RowLayout.PARENT_GRID)
            }
        }
    }

    @ApiStatus.Internal
    class FileFilter(private val fileNames: List<String>) : Condition<VirtualFile> {
        override fun value(t: VirtualFile?): Boolean {
            return fileNames.contains(t?.name ?: return false)
        }
    }

    companion object {
        const val ID = "Settings.Mypy"
    }
}
