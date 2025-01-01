package works.szabope.plugins.mypy.testutil

import com.jetbrains.python.sdk.PyRemoteSdkAdditionalDataMarker
import com.jetbrains.python.sdk.PythonSdkAdditionalData
import org.jdom.Element

class PyRemoteSdkAdditionalDataMock : PythonSdkAdditionalData(), PyRemoteSdkAdditionalDataMarker {
    override fun save(rootElement: Element) = Unit
}