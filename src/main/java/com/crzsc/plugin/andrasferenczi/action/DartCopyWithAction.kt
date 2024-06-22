package com.crzsc.plugin.andrasferenczi.action

import com.crzsc.plugin.andrasferenczi.action.data.GenerationData
import com.crzsc.plugin.andrasferenczi.action.data.PerformAction
import com.crzsc.plugin.andrasferenczi.action.init.ActionData
import com.crzsc.plugin.andrasferenczi.action.utils.createCopyWithDeleteCall
import com.crzsc.plugin.andrasferenczi.action.utils.selectFieldsWithDialog
import com.crzsc.plugin.andrasferenczi.configuration.ConfigurationDataManager
import com.crzsc.plugin.andrasferenczi.declaration.allMembersFinal
import com.crzsc.plugin.andrasferenczi.declaration.fullTypeName
import com.crzsc.plugin.andrasferenczi.declaration.isNullable
import com.crzsc.plugin.andrasferenczi.declaration.variableName
import com.crzsc.plugin.andrasferenczi.ext.psi.extractClassName
import com.crzsc.plugin.andrasferenczi.templater.AliasedVariableTemplateParam
import com.crzsc.plugin.andrasferenczi.templater.AliasedVariableTemplateParamImpl
import com.crzsc.plugin.andrasferenczi.templater.CopyWithTemplateParams
import com.crzsc.plugin.andrasferenczi.templater.createCopyWithConstructorTemplate
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.lang.dart.psi.DartClassDefinition

class DartCopyWithAction : BaseAnAction() {

    override fun processAction(
        event: AnActionEvent,
        actionData: ActionData,
        dartClass: DartClassDefinition
    ): PerformAction? {
        val declarations = selectFieldsWithDialog(actionData.project, dartClass) ?: return null

        return processAction(
            GenerationData(actionData, dartClass, declarations)
        )
    }

    companion object : StaticActionProcessor {

        override fun processAction(generationData: GenerationData): PerformAction {
            val (actionData, dartClass, declarations) = generationData

            val (project, _, _, _) = actionData

            val variableNames: List<AliasedVariableTemplateParam> = declarations
                .map {
                    AliasedVariableTemplateParamImpl(
                        variableName = it.variableName,
                        type = it.fullTypeName
                            ?: throw RuntimeException("No type is available - this variable should not be assignable from constructor"),
                        publicVariableName = it.publicVariableName,
                        isNullable = it.isNullable
                    )
                }

            val templateManager = TemplateManager.getInstance(project)
            val configuration = ConfigurationDataManager.retrieveData(project)
            val dartClassName = dartClass.extractClassName()
            val generateOptimizedCopy = configuration.optimizeConstCopy && declarations.allMembersFinal()

            val template = createCopyWithConstructorTemplate(
                templateManager,
                CopyWithTemplateParams(
                    className = dartClassName,
                    variables = variableNames,
                    copyWithMethodName = configuration.copyWithMethodName,
                    useNewKeyword = configuration.useNewKeyword,
                    generateOptimizedCopy = generateOptimizedCopy,
                    nullSafety = configuration.nullSafety
                )
            )

            val copyWithDeleteCall = createCopyWithDeleteCall(
                dartClass,
                configuration.copyWithMethodName
            )

            return PerformAction(
                copyWithDeleteCall,
                template
            )
        }

    }
}