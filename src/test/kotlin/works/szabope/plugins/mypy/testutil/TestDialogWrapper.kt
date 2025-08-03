package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.ui.DialogWrapper
import works.szabope.plugins.mypy.dialog.MypyDialog

class TestDialogWrapper(private val lie: Class<out DialogWrapper>, vararg args: Any) : MypyDialog {
    private val arguments = args
    private var isShown = false
    private var exitCode: Int? = null

    override fun getWrappedClass() = lie

    override fun show() {
        isShown = true
    }

    override fun close(exitCode: Int) {
        this.exitCode = exitCode
    }

    override fun isShown(): Boolean = isShown

    override fun getExitCode(): Int? = exitCode

    override fun toString() = "${getWrappedClass().simpleName}\n${arguments.joinToString(",\n")}\n"
}