package com.crzsc.plugin.andrasferenczi.action

import com.crzsc.plugin.andrasferenczi.action.init.ActionData
import com.crzsc.plugin.andrasferenczi.action.data.GenerationData
import com.crzsc.plugin.andrasferenczi.action.data.PerformAction
import com.crzsc.plugin.andrasferenczi.action.data.combineAll
import com.crzsc.plugin.andrasferenczi.action.my.MyEqualsAction
import com.crzsc.plugin.andrasferenczi.action.my.MyHashCodeAction
import com.crzsc.plugin.andrasferenczi.action.my.MyToStringAction
import com.crzsc.plugin.andrasferenczi.ext.addNewLine
import com.crzsc.plugin.andrasferenczi.ext.addSpace
import com.crzsc.plugin.andrasferenczi.ext.createDartTemplate
import com.crzsc.plugin.andrasferenczi.ext.psi.commentContent
import com.crzsc.plugin.andrasferenczi.ext.psi.findChildrenByType
import com.crzsc.plugin.andrasferenczi.ext.psi.isTopClassLevel
import com.crzsc.plugin.andrasferenczi.templater.TemplateConstants
import com.crzsc.plugin.andrasferenczi.templater.TemplateType
import com.crzsc.plugin.andrasferenczi.traversal.TraversalType
import com.crzsc.plugin.andrasferenczi.action.BaseAnAction
import com.crzsc.plugin.andrasferenczi.action.DartCopyWithAction
import com.crzsc.plugin.andrasferenczi.action.MapAction
import com.crzsc.plugin.andrasferenczi.action.NamedArgumentConstructorAction
import com.crzsc.plugin.andrasferenczi.action.utils.selectFieldsWithDialog
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiComment
import com.jetbrains.lang.dart.psi.DartClassDefinition

class FullDataAction : BaseAnAction() {

    override fun processAction(
        event: AnActionEvent,
        actionData: ActionData,
        dartClass: DartClassDefinition
    ): PerformAction? {
        val project = actionData.project
        val declarations = selectFieldsWithDialog(project, dartClass)
            ?: return null

        val generationData = GenerationData(actionData, dartClass, declarations)

        val templateManager = TemplateManager.getInstance(project)

        val processActions = listOf(
            NamedArgumentConstructorAction.Companion,
            MyEqualsAction.Companion,
            MyHashCodeAction.Companion,
            MyToStringAction.Companion,
            DartCopyWithAction.Companion,
            MapAction.Companion
        )
            .map { it.processAction(generationData) }

        return listOf(
            // TODO
            processCommentBeginAction(dartClass, templateManager),
            *processActions.toTypedArray(),
            processCommentEndAction(templateManager)
        )
            .combineAll()

        // transactionguard
    }

    companion object {

        private fun processCommentBeginAction(
            dartClass: DartClassDefinition,
            templateManager: TemplateManager
        ): PerformAction {
            return PerformAction(
                createDeleteCall(dartClass),
                templateManager.createCommentBeginTemplate()
            )
        }

        private fun processCommentEndAction(
            templateManager: TemplateManager
        ): PerformAction {
            return PerformAction(
                // The begin has already deleted the items
                null,
                templateManager.createCommentEndTemplate()
            )
        }

        private fun createDeleteCall(dartClass: DartClassDefinition): (() -> Unit)? {
            val comments = extractSectionComments(
                dartClass,
                createCommentBeginContent(),
                createCommentEndContent()
            )

            if (comments.isEmpty()) {
                return null
            }

            return { comments.forEach { it.delete() } }
        }

        /**
         * Tries to extract the matching comments and in case of missing comments it returns empty
         */
        private fun extractSectionComments(
            dartClass: DartClassDefinition,
            commentBeginContent: String,
            commentEndContent: String
        ): List<PsiComment> {

            // Breadth does not give the correct order - the end comment might be higher level in the hierarchy
            val comments = dartClass.findChildrenByType<PsiComment>(traversalType = TraversalType.Depth)
                // Top level only
                .filter { it.isTopClassLevel }
                .toList()

            val beginCommentIndex = comments.indexOfFirst { it.commentContent == commentBeginContent }

            val beginComment = comments.getOrNull(beginCommentIndex) ?: return emptyList()

            // End comment will be searched after the first one (the retrieval of these children are in order)
            val endComment = comments
                .subList(beginCommentIndex, comments.size)
                .firstOrNull { it.commentContent == commentEndContent }
                ?: return emptyList()

            return listOf(beginComment, endComment)
        }

        private fun TemplateManager.createCommentBeginTemplate(): Template {
            return createDartTemplate(TemplateType.Comment)
                .apply {
                    addNewLine()
                    addSpace()
                    addSpace()
                    addTextSegment("//")
                    addTextSegment(createCommentBeginContent())
                    addNewLine()
                }
        }

        private fun TemplateManager.createCommentEndTemplate(): Template {
            return createDartTemplate(TemplateType.Comment)
                .apply {
                    addNewLine()
                    addSpace()
                    addSpace()
                    addTextSegment("//")
                    addTextSegment(createCommentEndContent())
                    addNewLine()
                }
        }

        private fun createCommentBeginContent(): String =
            "<editor-fold desc=\"${TemplateConstants.EDITOR_FOLD_DATA_CLASS_NAME}\">"

        private fun createCommentEndContent(): String = "</editor-fold>"

    }
}