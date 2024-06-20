package com.crzsc.plugin.git

import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.Charset

object LineSeparatorHelper {

    enum class LINE_SEPARATOR {
        WINDOWS, LINUX, MAC, UNKNOWN
    }

    @Throws(IllegalArgumentException::class)
    fun getLineSeparator(f: File): LINE_SEPARATOR {
        if (!f.isFile || !f.exists()) {
            throw IllegalArgumentException("file must exist!")
        }

        var raf: RandomAccessFile? = null
        try {
            raf = RandomAccessFile(f, "r")
            val line = raf.readLine() ?: return LINE_SEPARATOR.UNKNOWN

            // 必须执行这一步，因为 RandomAccessFile 的 readLine() 会自动忽略并跳过换行符，所以需要先回退文件指针位置
            // "ISO-8859-1" 为 RandomAccessFile 使用的字符集，此处必须指定，否则中文 length 获取不对 
            raf.seek(line.toByteArray(Charsets.ISO_8859_1).size.toLong())

            var nextByte = raf.readByte()
            if (nextByte.toInt() == 0x0A) {
                return LINE_SEPARATOR.LINUX
            }

            if (nextByte.toInt() != 0x0D) {
                return LINE_SEPARATOR.UNKNOWN
            }

            return try {
                nextByte = raf.readByte()
                if (nextByte.toInt() == 0x0A) {
                    LINE_SEPARATOR.WINDOWS
                } else {
                    LINE_SEPARATOR.MAC
                }
            } catch (e: EOFException) {
                LINE_SEPARATOR.MAC
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            raf?.close()
        }

        return LINE_SEPARATOR.UNKNOWN
    }

    @Suppress("ResultOfMethodCallIgnored")
    fun convert(oldLs: LINE_SEPARATOR?, f: File?, charsets: Charset?): Boolean {
        if (oldLs == null || oldLs == LINE_SEPARATOR.UNKNOWN) {
            return false
        }

        if (f == null || !f.isFile || !f.exists()) {
            return false
        }

        val actualCharset: Charset = charsets ?: Charsets.UTF_8

        val newLs = getLineSeparator(f)
        if (newLs == oldLs) {
            return false
        }

        try {
            val lines = mutableListOf<String>()
            f.bufferedReader(charset = actualCharset).use { br ->
                br.forEachLine { line ->
                    lines.add(line)
                }
            }

            f.bufferedWriter(charset = actualCharset).use { bw ->
                var lineNumber = 0
                lines.forEach { line ->
                    if (lineNumber != 0) {
                        when (oldLs) {
                            LINE_SEPARATOR.WINDOWS -> bw.append("\r\n")
                            LINE_SEPARATOR.LINUX -> bw.append("\n")
                            LINE_SEPARATOR.MAC -> bw.append("\r")
                            else -> {}
                        }
                    }
                    bw.write(line)
                    lineNumber++
                }
            }

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }
}
