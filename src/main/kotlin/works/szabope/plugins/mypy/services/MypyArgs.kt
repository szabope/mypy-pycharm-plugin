package works.szabope.plugins.mypy.services

class MypyArgs {
    companion object {
        const val MYPY_RECOMMENDED_COMMAND_ARGS = "--follow-imports silent"
        const val MYPY_MANDATORY_COMMAND_ARGS = "--show-column-numbers --show-absolute-path --output json"
    }
}