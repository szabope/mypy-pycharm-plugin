package works.szabope.plugins.mypy

import com.intellij.openapi.ui.DialogWrapper
import works.szabope.plugins.mypy.dialog.MypyDialog

class TestDialogWrapper(private val lie: Class<out DialogWrapper>) : MypyDialog {
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
}