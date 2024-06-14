package com.crzsc.plugin.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.NotNull

class MapModificationListener : StartupActivity {
    override fun runActivity(@NotNull project: Project) {
        println("MapModificationListener started")
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val document: Document = event.document
                val psiFile: PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(document)

                if (psiFile != null && psiFile.name.endsWith(".dart")) {
                    val newContent = event.newFragment.toString()
                    val colorKey = extractColorKey(newContent)

                    if (colorKey != null) {
                        val mapEntry = "    AppColorsKey.$colorKey: Color(0x${colorKey.substringAfter("color")}),\n"
                        val classEntry = "  static Color $colorKey = _showColor(AppColorsKey.$colorKey);\n"
                        ApplicationManager.getApplication().invokeLater {
                            WriteCommandAction.runWriteCommandAction(project) {
                                // 在这里执行实际的文件修改操作
                                insertIntoMap(document, mapEntry)
                                insertIntoClass(document, classEntry)
                            }
                        }
                    }
                }
            }

            private fun insertIntoMap(document: Document, mapEntry: String) {
                // 获取文档对象，并进行插入操作
                val mapInsertPosition = findMapInsertPosition(document)
                val mapLineNumber = document.getLineNumber(mapInsertPosition);
                if (mapInsertPosition != -1 ) {
                    document.insertString(mapInsertPosition, mapEntry)
                }
            }

            private fun insertIntoClass(document: Document, classEntry: String) {
                // 获取文档对象，并进行插入操作
                val classInsertPosition = findClassInsertPosition(document)
                val classLineNumber = document.getLineNumber(classInsertPosition);

                if (classInsertPosition != -1) {
                    document.insertString(classInsertPosition, classEntry)
                }
            }

            private fun extractColorKey(newContent: String): String? {
                // 假设新内容格式是 `static const String colorXXXXXX = "colorXXXXXX";`
                return if (newContent.contains("static const String") && newContent.contains("=")) {
                    val startIndex = newContent.indexOf("color")
                    val endIndex = newContent.indexOf("=", startIndex)
                    if (startIndex != -1 && endIndex != -1) {
                        newContent.substring(startIndex, endIndex).trim()
                    } else null
                } else null
            }

            private fun findMapInsertPosition(document: Document): Int {
                val fileContent = document.text
                val targetMap = "static const Map<String, Color> whiteColorsInfo = {"
                val mapStartIndex = fileContent.indexOf(targetMap)
                return if (mapStartIndex != -1) {
                    val mapEndIndex = fileContent.indexOf("}", mapStartIndex)
                    // 获取 map 结尾行号的下一行
                    (document.getLineEndOffset(document.getLineNumber(mapEndIndex)-1) +1)
                } else -1
            }

            private fun findClassInsertPosition(document: Document): Int {
                val fileContent = document.text
                val targetClass = "class ColorsExt {"
                val classStartIndex = fileContent.indexOf(targetClass)
                return if (classStartIndex != -1) {
                    val classEndIndex = fileContent.lastIndexOf("_showColor(AppColorsKey")
                    // 获取最后一个 _showColor 方法所在行的下一行
                    (document.getLineEndOffset(document.getLineNumber(classEndIndex)) +1)

                } else -1
            }

        }, project)
    }
}
