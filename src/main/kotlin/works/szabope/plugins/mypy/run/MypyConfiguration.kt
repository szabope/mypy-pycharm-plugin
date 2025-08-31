package works.szabope.plugins.mypy.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project
import com.jetbrains.python.run.PythonConfigurationFactoryBase
import com.jetbrains.python.run.PythonRunConfiguration

class MypyConfigurationType : ConfigurationType {
    val configurationFactory = MypyConfigurationFactory(this)

    // this thing should never appear on screen
    override fun getDisplayName() = "Mypy"
    override fun getConfigurationTypeDescription() = "Mypy run configuration"
    override fun getIcon() = DummyIcon(16)

    override fun getId() = "Mypy"
    override fun isManaged() = false
    override fun getConfigurationFactories() = arrayOf<ConfigurationFactory>(configurationFactory)

    companion object {
        @JvmStatic
        val INSTANCE = MypyConfigurationType()
    }
}

class MypyConfigurationFactory(type: ConfigurationType) : PythonConfigurationFactoryBase(type) {
    fun createConfiguration(project: Project, name: String) = MypyRunConfiguration(project, this, name)
    override fun createTemplateConfiguration(project: Project) = createConfiguration(project, "Mypy Template")
    override fun getId() = "Mypy"
}

class MypyRunConfiguration(project: Project, factory: ConfigurationFactory, configurationName: String) :
    PythonRunConfiguration(project, factory) {

    init {
        setUnbufferedEnv()
        name = configurationName
    }

    override fun checkConfiguration() = Unit
}
