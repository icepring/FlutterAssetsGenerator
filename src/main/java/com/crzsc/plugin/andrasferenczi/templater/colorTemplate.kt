package com.crzsc.plugin.andrasferenczi.templater

import com.crzsc.plugin.andrasferenczi.ext.*
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager

data class ColorTemplateParams(
    val className: String,
    val variables: List<AliasedVariableTemplateParam>,
)

// The 2 will be generated with the same function
fun createColorTemplate(
    templateManager: TemplateManager,
    params: ColorTemplateParams
): List<Template> {

    return listOf(templateManager.createTemplate(
        TemplateType.Color.templateKey,
        TemplateConstants.GENERATE_COLOR_METHOD_NAME
    ).apply {
        addToAppColorsInfo(params)
    }, templateManager.createTemplate(
        TemplateType.Color.templateKey,
        TemplateConstants.GENERATE_COLOR_METHOD_NAME
    ).apply {
        addColorsExt(params)
    })
}

fun findOffset():List<Int>{
    return listOf()
}

private fun Template.addToAppColorsInfo(params: ColorTemplateParams) {
    val (colorValue, variables) = params

    isToReformat = true
    variables.forEach {
        it.mapKeyString.also { keyParam ->
            addTextSegment("AppColorsKey.$keyParam:Color(0x${keyParam.replace("color","")}),\n")
        }
    }
}

private fun Template.addColorsExt(params: ColorTemplateParams) {
    val (colorValue, variables) = params

    isToReformat = true
    variables.forEach {
        it.mapKeyString.also { keyParam ->
            addTextSegment("static Color $keyParam= _showColor(AppColorsKey.$keyParam);\n")
        }
    }
}


