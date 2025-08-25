package works.szabope.plugins.mypy.services

import works.szabope.plugins.common.services.ImmutableSettingsData

data class MypyExecutorConfiguration(
    override val executablePath: String?,
    override val useProjectSdk: Boolean,
    override val configFilePath: String? = null,
    override val arguments: String? = null,
    override val projectDirectory: String? = null,
    override val excludeNonProjectFiles: Boolean,
    override val scanBeforeCheckIn: Boolean
) : ImmutableSettingsData