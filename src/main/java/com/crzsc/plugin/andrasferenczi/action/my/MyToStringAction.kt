package com.crzsc.plugin.andrasferenczi.action.my

import com.crzsc.plugin.andrasferenczi.action.StaticActionProcessor
import com.crzsc.plugin.andrasferenczi.action.data.GenerationData
import com.crzsc.plugin.andrasferenczi.action.data.PerformAction
import com.crzsc.plugin.andrasferenczi.declaration.isNullable
import com.crzsc.plugin.andrasferenczi.declaration.variableName
import com.crzsc.plugin.andrasferenczi.ext.psi.extractClassName
import com.crzsc.plugin.andrasferenczi.ext.psi.findMethodsByName
import com.crzsc.plugin.andrasferenczi.templater.NamedVariableTemplateParamImpl
import com.crzsc.plugin.andrasferenczi.templater.TemplateConstants
import com.crzsc.plugin.andrasferenczi.templater.ToStringTemplateParams
import com.crzsc.plugin.andrasferenczi.templater.createToStringTemplate
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClassDefinition

class MyToStringAction {
    companion object : StaticActionProcessor {

        private fun createDeleteCall(dartClass: DartClassDefinition): (() -> Unit)? {
            val toString = dartClass.findMethodsByName(TemplateConstants.TO_STRING_METHOD_NAME)
                .firstOrNull()
                ?: return null

            return { toString.delete() }
        }

        override fun processAction(generationData: GenerationData): PerformAction? {
            val (actionData, dartClass, declarations) = generationData

            val project = actionData.project

            val templateManager = TemplateManager.getInstance(project)
            val dartClassName = dartClass.extractClassName()

            val template = createToStringTemplate(
                templateManager = templateManager,
                params = ToStringTemplateParams(
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
