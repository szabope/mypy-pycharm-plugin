package works.szabope.plugins.mypy.configurable

import works.szabope.plugins.mypy.MypyBundle
import java.io.File

class MypyConfigFileValidator {
    fun validateConfigFilePath(path: String?): String? {
        if (path == null) return null
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            return MypyBundle.message("mypy.configuration.path_to_config_file.not_exists")
        }
        if (file.isDirectory) {
            return MypyBundle.message("mypy.configuration.path_to_config_file.is_directory")
        }
        return null
    }
}