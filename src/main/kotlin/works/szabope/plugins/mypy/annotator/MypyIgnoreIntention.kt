package works.szabope.plugins.mypy.annotator

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import com.jetbrains.python.ast.impl.PyUtilCore
import com.jetbrains.python.psi.PyUtil.StringNodeInfo
import com.jetbrains.python.psi.impl.PyPsiUtils
import works.szabope.plugins.mypy.MyBundle

// TODO: test https://plugins.jetbrains.com/docs/intellij/modifying-psi.html#maintaining-tree-structure-consistency
// To make sure you're not introducing inconsistencies, you can call PsiTestUtil.checkFileStructure()
// in the tests for your action that modifies the PSI. This method ensures that the structure you've built is the same
// as what the parser produces.

/**
 * Intention action to append `# type: ignore` comment to suppress Mypy annotations.
 */
class MypyIgnoreIntention(private val line: Int) : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getText(): String {
        return MyBundle.message("mypy.intention.ignore.text")
    }

    override fun getFamilyName(): String {
        return MyBundle.message("mypy.intention.ignore.family_name")
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return isTypeIgnoreCommandPresent(element) && !isTripleQuotedMultilineString(element)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val existingComment = PyPsiUtils.findSameLineComment(element)
        val lineEndOffset = existingComment?.startOffset ?: element.containingFile.fileDocument.getLineEndOffset(line)
        val spaces = " ".repeat(existingComment?.let { 0 } ?: 2)
        element.containingFile.fileDocument.insertString(lineEndOffset, "$spaces# type: ignore ")
    }

    @Suppress("UnstableApiUsage")
    private fun isTypeIgnoreCommandPresent(element: PsiElement) =
        "ignore" != PyPsiUtils.findSameLineComment(element)?.text?.let { PyUtilCore.getTypeCommentValue(it) }

    /** mypy does not support `#type: ignore` on multiline triple quoted elements */
    private fun isTripleQuotedMultilineString(element: PsiElement): Boolean {
        try {
            val stringNode = element.context?.let { StringNodeInfo(it) } ?: return false
            return stringNode.isTripleQuoted && stringNode.content.contains('\n')
        } catch (e: IllegalArgumentException) {
            return false
        }
    }
}