package com.crzsc.plugin.andrasferenczi.ext

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiFile

/**
 *
 * @author icepring
 * @date 21/06/2024
 */

fun PsiFile.findLineOffset(start: String, end: String): Int {
    val document = FileDocumentManager.getInstance().getDocument(virtualFile)
    return document?.run {
        val mapStartIndex = text.indexOf(start)
        if (mapStartIndex != -1) {
            var mapEndIndex = text.indexOf(end, mapStartIndex)
            var offset = -1
            if (mapEndIndex == -1) {
                mapEndIndex = mapStartIndex;
                offset = 0
            }
            // 获取 map 结尾行号的下一行
            (document.getLineEndOffset(document.getLineNumber(mapEndIndex) + offset) + 1)
        } else -1
    } ?: -1
}

fun PsiFile.contains(content: String): Boolean {
    val document = FileDocumentManager.getInstance().getDocument(virtualFile)
    return document?.run {
        text.contains(content)
    } ?: false
}

