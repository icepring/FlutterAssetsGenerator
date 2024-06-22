package com.crzsc.plugin.andrasferenczi.action.my

import com.crzsc.plugin.andrasferenczi.action.StaticActionProcessor
import com.crzsc.plugin.andrasferenczi.action.data.GenerationData
import com.crzsc.plugin.andrasferenczi.action.data.PerformAction
import com.crzsc.plugin.andrasferenczi.declaration.isNullable
import com.crzsc.plugin.andrasferenczi.declaration.variableName
import com.crzsc.plugin.andrasferenczi.ext.psi.extractClassName
import com.crzsc.plugin.andrasferenczi.ext.psi.findMethodsByName
import com.crzsc.plugin.andrasferenczi.templater.EqualsTemplateParams
import com.crzsc.plugin.andrasferenczi.templater.NamedVariableTemplateParamImpl
import com.crzsc.plugin.andrasferenczi.templater.TemplateConstants
import com.crzsc.plugin.andrasferenczi.templater.createEqualsTemplate
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClassDefinition

class MyEqualsAction {

    companion object : StaticActionProcessor {

        private fun createDeleteCall(dartClass: DartClassDefinition): (() -> Unit)? {
            val equals = dartClass.findMethodsByName(TemplateConstants.EQUALS_OPERATOR_METHOD_NAME)
                .firstOrNull()
                ?: return null

            return { equals.delete() }
        }

        override fun processAction(generationData: GenerationData): PerformAction? {
            val (actionData, dartClass, declarations) = generationData

            val project = actionData.project

            val templateManager = TemplateManager.getInstance(project)
            val dartClassName = dartClass.extractClassName()

            val template = createEqualsTemplate(
                templateManager,
                EqualsTemplateParams(
                    className = dartClassName,
                    variables = declarations.map {
                        NamedVariableTemplateParamImpl(
                            it.variableName,
                            isNullable = it.isNullable
                        )
                    }
                )
            )

            return PerformAction(
                createDeleteCall(dartClass),
                template
            )
        }
    }

}