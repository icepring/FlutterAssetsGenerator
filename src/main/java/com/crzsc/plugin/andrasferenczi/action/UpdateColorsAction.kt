package com.crzsc.plugin.andrasferenczi.action

import com.crzsc.plugin.andrasferenczi.action.data.GenerationData
import com.crzsc.plugin.andrasferenczi.action.data.PerformAction
import com.crzsc.plugin.andrasferenczi.action.init.ActionData
import com.crzsc.plugin.andrasferenczi.configuration.ConfigurationDataManager
import com.crzsc.plugin.andrasferenczi.declaration.DeclarationExtractor
import com.crzsc.plugin.andrasferenczi.declaration.fullTypeName
import com.crzsc.plugin.andrasferenczi.declaration.isNullable
import com.crzsc.plugin.andrasferenczi.declaration.variableName
import com.crzsc.plugin.andrasferenczi.ext.psi.extractClassName
import com.crzsc.plugin.andrasferenczi.templater.AliasedVariableTemplateParam
import com.crzsc.plugin.andrasferenczi.templater.AliasedVariableTemplateParamImpl
import com.crzsc.plugin.andrasferenczi.templater.ColorTemplateParams
import com.crzsc.plugin.andrasferenczi.templater.createColorTemplate
import com.crzsc.plugin.andrasferenczi.ext.contains
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.lang.dart.psi.DartClassDefinition

class UpdateColorsAction : BaseAnAction() {
    override fun processAction(
        event: AnActionEvent,
        actionData: ActionData,
        dartClass: DartClassDefinition
    ): PerformAction {
        val declarations = DeclarationExtractor.extractDeclarationsFromClass(dartClass)

        return Companion.processAction(
            GenerationData(actionData, dartClass, declarations)
        )
    }

    companion object : StaticActionProcessor {
        override fun processAction(generationData: GenerationData): PerformAction {
            val (actionData, dartClass, declarations) = generationData

            val (project, _, dartFile, _) = actionData
            val variableNames: List<AliasedVariableTemplateParam> = declarations
                .map {
                    AliasedVariableTemplateParamImpl(
                        variableName = it.variableName,
                        type = it.fullTypeName
                            ?: throw RuntimeException("No type is available - this variable should not be assignable from constructor"),
                        publicVariableName = it.publicVariableName,
                        isNullable = it.isNullable
                    )
                }.filter {
                    !dartFile.contains("AppColorsKey.${it.variableName}")
                }
            val templateManager = TemplateManager.getInstance(project)
            val configuration = ConfigurationDataManager.retrieveData(project)
            val dartClassName = dartClass.extractClassName()

            val template = createColorTemplate(
                templateManager,
                ColorTemplateParams(
                    className = dartClassName,
                    variables = variableNames
                )
            )
            return PerformAction(
                null,
                template, true
            )
        }

    }
}
