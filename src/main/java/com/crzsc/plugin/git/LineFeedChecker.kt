package com.crzsc.plugin.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.IOException

class LineFeedChecker(private val project: Project) {

    fun checkLineFeeds(files: MutableCollection<VirtualFile>): Map<VirtualFile, String> {
        val result = mutableMapOf<VirtualFile, String>()
        for (file in files) {
            if (file.isDirectory) continue

            try {
                val lineSeparator = LineSeparatorHelper.getLineSeparator(File(file.path))
                val fileType = when (lineSeparator) {
                    LineSeparatorHelper.LINE_SEPARATOR.WINDOWS -> "CRLF"
                    LineSeparatorHelper.LINE_SEPARATOR.LINUX -> "LF"
                    LineSeparatorHelper.LINE_SEPARATOR.MAC -> "CR"
                    else -> "Unknown"
                }
                result[file] = fileType
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun convertLineFeeds(file: VirtualFile, toFormat: String) {
        val newContent = when (toFormat) {
            "CR" -> LineSeparatorHelper.convert(LineSeparatorHelper.LINE_SEPARATOR.MAC, File(file.path), null)
            "LF" -> LineSeparatorHelper.convert(LineSeparatorHelper.LINE_SEPARATOR.LINUX, File(file.path), null)
            "CRLF" -> LineSeparatorHelper.convert(LineSeparatorHelper.LINE_SEPARATOR.WINDOWS, File(file.path), null)
            else -> return
        }

    }
}
