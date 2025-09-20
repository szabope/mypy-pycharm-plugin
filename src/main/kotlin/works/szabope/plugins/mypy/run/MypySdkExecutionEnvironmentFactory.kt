package works.szabope.plugins.mypy.run

import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.project.Project
import com.jetbrains.python.sdk.pythonSdk

class MypySdkExecutionEnvironmentFactory(private val project: Project) {

    fun createEnvironment(parameters: List<String>, workingDirectory: String? = null): ExecutionEnvironment {
        val configurationFactory = MypyConfigurationType.INSTANCE.configurationFactory
        val conf = configurationFactory.createConfiguration(project, "mypy")
        conf.sdk = project.pythonSdk
        conf.workingDirectory = workingDirectory
        conf.setAddContentRoots(true)
        conf.setAddSourceRoots(true)
        conf.scriptName = "mypy"
        conf.scriptParameters = parameters.joinToString(" ")
        conf.isModuleMode = true
        val settings = RunManager.getInstance(project).createConfiguration(conf, configurationFactory)
        settings.isActivateToolWindowBeforeRun = false
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        return ExecutionEnvironmentBuilder.create(executor, settings).build()
    }
}
