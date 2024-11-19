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
import works.szabope.plugins.mypy.services.MypyArgs
import works.szabope.plugins.mypy.services.MypySettings

internal class MypySettingConfigurable(private val project: Project) : BoundSearchableConfigurable(
    MyBundle.message("mypy.configurable.name"), MyBundle.message("mypy.configurable.name"), _id = ID
), Configurable.NoScroll {

    private val settings
        get() = MypySettings.getInstance(project)

    private val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false).withFileFilter(
        FileFilter(
            * if (SystemInfo.isWindows) {
                arrayOf("mypy.exe", "mypyc.exe")
            } else {
                arrayOf("mypy", "mypyc")
            }
        )
    )

    override fun createPanel(): DialogPanel {
        return panel {
            indent {
                row {
                    label(MyBundle.message("mypy.settings.path_to_executable.label"))
                    textFieldWithBrowseButton(
                        project = project, fileChooserDescriptor = fileChooserDescriptor
                    ).align(Align.FILL).bindText(
                        getter = { settings.mypyExecutable.orEmpty() },
                        setter = { settings.mypyExecutable = it.trimToNull() },
                    ).validationOnInput {
                        if (it.text.isBlank()) {
                            return@validationOnInput warning(MyBundle.message("mypy.settings.path_to_executable.empty_warning"))
                        }
                        try {
                            settings.validateMypyExecutable(it.text.trimToNull())
                        } catch (e: MypySettings.MypyConfigurationValidationException) {
                            return@validationOnInput error(e.message ?: "N/A")
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
                    ).validationOnInput {
                        try {
                            settings.validateConfigFile(it.text.trimToNull())
                        } catch (e: MypySettings.MypyConfigurationValidationException) {
                            return@validationOnInput error(e.message ?: "N/A")
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
            }
        }
    }

    @ApiStatus.Internal
    class FileFilter(private val fileNames: List<String>) : Condition<VirtualFile> {
        constructor(vararg fileNames: String) : this(fileNames.toList())

        override fun value(t: VirtualFile?): Boolean {
            return fileNames.contains(t?.name ?: return false)
        }
    }

    companion object {
        const val ID = "Settings.Mypy"
    }
}