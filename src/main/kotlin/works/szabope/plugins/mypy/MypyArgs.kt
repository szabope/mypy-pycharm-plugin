package works.szabope.plugins.mypy

object MypyArgs {
    const val MYPY_RECOMMENDED_COMMAND_ARGS = "--follow-imports silent --exclude \\.pyi\$"
    val MYPY_MANDATORY_COMMAND_ARGS: List<String> = listOf("--show-column-numbers", "--show-absolute-path", "--output", "json")
}