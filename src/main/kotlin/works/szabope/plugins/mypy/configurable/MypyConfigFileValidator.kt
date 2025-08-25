package works.szabope.plugins.mypy.configurable

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import works.szabope.plugins.mypy.MypyBundle
import java.io.File

class MypyConfigFileValidator {
    fun validateConfigFilePath(path: String?, builder: ValidationInfoBuilder): ValidationInfo? {
        if (path == null) return null
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            return builder.error(MypyBundle.message("mypy.configuration.path_to_config_file.not_exists"))
        }
        if (file.isDirectory) {
            return builder.error(MypyBundle.message("mypy.configuration.path_to_config_file.is_directory"))
        }
        return null
    }
}