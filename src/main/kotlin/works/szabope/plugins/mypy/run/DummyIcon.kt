package works.szabope.plugins.mypy.run

import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

class DummyIcon(private val size: Int) : Icon {
    override fun paintIcon(p0: Component?, p1: Graphics?, p2: Int, p3: Int) = Unit
    override fun getIconWidth() = size
    override fun getIconHeight() = size
}