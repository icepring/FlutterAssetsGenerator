package com.crzsc.plugin.actions

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartRecursiveVisitor
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression
import java.util.regex.Pattern

class DartColorScannerAction : AnAction() {
    val mappings = mutableMapOf<String, String>()
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) as? DartFile ?: return

        // 提取映射关系并查找并替换颜色映射关系
        val mappings = extractColorMappings(psiFile)
        println(mappings)

        // 创建并显示一个 ConsoleView
        val consoleView = ConsoleViewImpl(project, true)
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Dart Color Scanner")
        if (toolWindow != null) {
            val contentManager = toolWindow.contentManager
            val content = contentManager.factory.createContent(consoleView.component, "", false)
            contentManager.addContent(content)
            toolWindow.show(null)
        }

        // 后台任务扫描并替换 Dart 文件中的颜色代码
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Scanning Dart Colors") {
            override fun run(indicator: ProgressIndicator) {
                scanDartFiles(project, project.baseDir, consoleView, indicator)
            }

            override fun onFinished() {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showMessageDialog(project, "Color scanning and replacement completed.", "Info", Messages.getInformationIcon())
                }
            }
        })
    }



    private fun extractColorMappings(psiFile: DartFile): Map<String, String> {

        val fileText = psiFile.text

        // 正则表达式匹配颜色映射关系的格式，例如：grey1000: const Color(0xFF1E1E1E)
        val regex = Regex("""([a-zA-Z0-9_]+):\s*const\s*Color\s*\(\s*0x([0-9A-Fa-f]+)\s*\)""")

        // 找到所有匹配项，并将匹配的键值对放入映射中
        regex.findAll(fileText).forEach { matchResult ->
            val colorName = matchResult.groupValues[1]
            val hexValue = matchResult.groupValues[2]
            mappings[hexValue] = colorName
        }

        return mappings
    }


    private fun scanDartFiles(project: Project, dir: VirtualFile?, consoleView: ConsoleView, indicator: ProgressIndicator) {
        if (dir == null || !dir.isDirectory) return

        for (file in dir.children) {
            if (file.isDirectory && file.name == "lib") {
                scanDartFilesInDirectory(project, file, consoleView, indicator)
            } else if (file.isDirectory) {
                scanDartFiles(project, file, consoleView, indicator)
            }
        }
    }

    private fun scanDartFilesInDirectory(project: Project, dir: VirtualFile, consoleView: ConsoleView, indicator: ProgressIndicator) {
        for (file in dir.children) {
            if (file.isDirectory) {
                scanDartFilesInDirectory(project, file, consoleView, indicator)
            } else if (file.extension == "dart") {
                indicator.text = "Processing ${file.name}"
                consoleView.print("Processing ${file.path}\n", ConsoleViewContentType.NORMAL_OUTPUT)
                findAndReplaceColorsExtInFile(project, file, consoleView)
            }
        }
    }

    private fun findAndReplaceColorsExtInFile(project: Project, file: VirtualFile, consoleView: ConsoleView) {

        ApplicationManager.getApplication().invokeLater {
            val psiFile = PsiManager.getInstance(project).findFile(file) as? DartFile ?: return@invokeLater

            val fileContent = psiFile.text
            val pattern = Pattern.compile("ColorsExt\\.color([A-Fa-f0-9]{8})")
            val matcher = pattern.matcher(fileContent)
            var hasReplacements = false

            val newContent = StringBuffer()
            while (matcher.find()) {
                val colorCode = matcher.group(1)
                if(!mappings.containsKey(colorCode)){
                    continue
                }
                if (!hasReplacements) {
                    newContent.append("import 'package:shisankeisei/theme/color/theme_colors.dart';\n")
                    hasReplacements = true
                }
                matcher.appendReplacement(newContent, "ThemeColors.${mappings[colorCode]}")
            }
            if (!hasReplacements){
                return@invokeLater
            }

            matcher.appendTail(newContent)

            // 保存替换后的内容到文件
            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
            if (document != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    document.setText(newContent.toString())
                    // 格式化代码
                    PsiDocumentManager.getInstance(project).commitDocument(document)
                    val codeStyleManager = com.intellij.psi.codeStyle.CodeStyleManager.getInstance(project)
                    codeStyleManager.reformat(psiFile)
                }
            }
            consoleView.print("Completed ${file.path}\n", ConsoleViewContentType.NORMAL_OUTPUT)
        }


    }
}
