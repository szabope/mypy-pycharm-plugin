package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.projectRoots.*
import com.jetbrains.python.PyNames
import org.jdom.Element

class TestPythonLocalSdkType : SdkType(NAME) {

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) = Unit

    override fun suggestHomePath(): String? = null

    override fun isValidSdkHome(path: String): Boolean = true

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String) = NAME

    override fun createAdditionalDataConfigurable(
        sdkModel: SdkModel, sdkModificator: SdkModificator
    ): AdditionalDataConfigurable? = null

    override fun getPresentableName() = NAME

    companion object {
        const val NAME = PyNames.PYTHON_SDK_ID_NAME
    }
}