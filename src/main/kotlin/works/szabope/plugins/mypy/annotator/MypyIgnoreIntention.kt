package works.szabope.plugins.mypy.annotator

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.startOffset
import com.jetbrains.python.psi.PyFormattedStringElement
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyUtil.StringNodeInfo
import com.jetbrains.python.psi.impl.PyPsiUtils
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.parser.MypyMessage

/**
 * Intention action to append `# type: ignore[...]` comment to suppress Mypy annotations.
 */
class MypyIgnoreIntention(private val issue: MypyMessage) : PsiElementBaseIntentionAction(), IntentionAction {

    companion object {
        @JvmStatic
        val COMMENT_REGEX = "^#\\s+type:\\s+ignore(\\[(?<codes>[a-zA-Z\\s,-]+)])?".toRegex()
    }

    override fun getText(): String {
        return MypyBundle.message("mypy.intention.ignore.text", issue.code)
    }

    override fun getFamilyName(): String {
        return MypyBundle.message("mypy.intention.ignore.family_name")
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return !isTripleQuotedMultilineString(element)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val existingComment = PyPsiUtils.findSameLineComment(element)
        val existingMypyIgnoreComment = existingComment?.text?.let { COMMENT_REGEX.find(it) }
        val existingCodes = existingMypyIgnoreComment?.groups["codes"]?.value
        val comment = if (existingCodes != null) {
            existingMypyIgnoreComment.value.replace(existingCodes, "$existingCodes,${issue.code}")
        } else { // (existingMypyIgnoreComment != null) { IMPOSSIBLE, we cannot end up here with `#  type: ignore`
            "# type: ignore[${issue.code}]"
        }
        existingMypyIgnoreComment?.run {
            element.containingFile.fileDocument.deleteString(
                existingComment.textRange.startOffset, existingComment.textRange.endOffset
            )
        }
        val codeLineEndOffset =
            existingComment?.startOffset ?: element.containingFile.fileDocument.getLineEndOffset(issue.line)
        val spaces = " ".repeat(existingComment?.let { 0 } ?: 2)
        element.containingFile.fileDocument.insertString(codeLineEndOffset, "$spaces$comment ")
    }

    /** mypy does not support `#type: ignore` on multiline triple quoted elements */
    private fun isTripleQuotedMultilineString(element: PsiElement): Boolean {
        try {
            val elements = PsiTreeUtil.collectParents(element, PyFormattedStringElement::class.java, true) {
                it is PyStringLiteralExpression
            }
            return elements.any { StringNodeInfo(it).isTripleQuoted && it.text.contains('\n') }
        } catch (_: IllegalArgumentException) {
            return false
        }
    }
}