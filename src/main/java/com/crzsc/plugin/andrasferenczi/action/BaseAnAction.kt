package com.crzsc.plugin.andrasferenczi.action

import com.crzsc.plugin.andrasferenczi.action.data.PerformAction
import com.crzsc.plugin.andrasferenczi.action.init.ActionData
import com.crzsc.plugin.andrasferenczi.action.init.tryCreateActionData
import com.crzsc.plugin.andrasferenczi.action.init.tryExtractDartClassDefinition
import com.crzsc.plugin.andrasferenczi.ext.*
import com.crzsc.plugin.andrasferenczi.ext.findLineOffset
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiDocumentManager
import com.jetbrains.lang.dart.psi.DartClassDefinition
import org.jetbrains.kotlin.idea.core.moveCaret

abstract class BaseAnAction : AnAction() {

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible =
            event.extractOuterDartClass() !== null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    final override fun actionPerformed(event: AnActionEvent) {
        val actionData = tryCreateActionData(event) ?: return
        val dartClass = tryExtractDartClassDefinition(actionData) ?: return

        val performAction = this.processAction(
            event,
            actionData,
            dartClass
        ) ?: return

        val (project, editor, dartFile, _) = actionData

        val templateManager = TemplateManager.getInstance(project)

        project.runWriteAction {
            performAction.deleteAction?.let {
                it.invoke()

                PsiDocumentManager.getInstance(project)
                    .doPostponedOperationsAndUnblockDocument(editor.document)
            }

            val templates = performAction.templatesToAdd
            val offset = performAction.offset


            if (!offset) {
                editor.setCaretToEndOfTheClass(dartClass)
            }

            // Todo: Set caret at the end of each template (?)

            if (!offset) {
                templateManager.startTemplate(editor, templateManager.createSeparatorTemplate())
            }

            templates
                .filter { it.templateText.isNotEmpty() }
                .forEachIndexed { index, template ->
                if (offset) {
                    if (dartClass.name == "AppColorsKey") {
                        val start = if (index == 0) {
                            "static const Map<String, Color> whiteColorsInfo = {"
                        } else {
                            "class ColorsExt {"
                        }
                        val end = if (index == 0) {
                            "}"
                        } else {
                            "_showColor(AppColorsKey"
                        }
                        dartFile.findLineOffset(start, end).let {
                            if (it > 0) {
                                editor.moveCaret(it)
                            }
                        }
                        templateManager.startTemplate(editor, template)
                    }
                } else {
                    templateManager.startTemplate(editor, template)
                }

                if (index != templates.lastIndex) {
                    if (!offset) {
                        templateManager.startTemplate(editor, templateManager.createSeparatorTemplate())
                    }
                }
            }

        }
    }

    protected abstract fun processAction(
        event: AnActionEvent,
        actionData: ActionData,
        dartClass: DartClassDefinition
    ): PerformAction?

}