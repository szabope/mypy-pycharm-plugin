package works.szabope.plugins.mypy

object MypyArgs {
    const val MYPY_RECOMMENDED_COMMAND_ARGS = "--follow-imports silent --exclude \\.pyi\$"
    const val MYPY_MANDATORY_COMMAND_ARGS = "--show-column-numbers --show-absolute-path --output json"
}