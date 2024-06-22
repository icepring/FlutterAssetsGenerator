package com.crzsc.plugin.andrasferenczi.action.my

import com.crzsc.plugin.andrasferenczi.action.StaticActionProcessor
import com.crzsc.plugin.andrasferenczi.action.data.GenerationData
import com.crzsc.plugin.andrasferenczi.action.data.PerformAction
import com.crzsc.plugin.andrasferenczi.declaration.isNullable
import com.crzsc.plugin.andrasferenczi.declaration.variableName
import com.crzsc.plugin.andrasferenczi.ext.psi.findChildrenByType
import com.crzsc.plugin.andrasferenczi.templater.HashCodeTemplateParams
import com.crzsc.plugin.andrasferenczi.templater.NamedVariableTemplateParamImpl
import com.crzsc.plugin.andrasferenczi.templater.TemplateConstants
import com.crzsc.plugin.andrasferenczi.templater.createHashCodeTemplate
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartGetterDeclaration

class MyHashCodeAction {

    companion object : StaticActionProcessor {

        private fun createDeleteCall(dartClass: DartClassDefinition): (() -> Unit)? {
            val hashCode = dartClass.findChildrenByType<DartGetterDeclaration>()
                .filter { it.name == TemplateConstants.HASHCODE_NAME }
                .firstOrNull()
                ?: return null

            return { hashCode.delete() }
        }

        override fun processAction(generationData: GenerationData): PerformAction? {
            val (actionData, dartClass, declarations) = generationData

            val project = actionData.project

            val templateManager = TemplateManager.getInstance(project)

            val template = createHashCodeTemplate(
                templateManager = templateManager,
                params = HashCodeTemplateParams(
                    declarations.map {
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
