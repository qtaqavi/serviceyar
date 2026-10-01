package com.example

import org.junit.Assert.*
import org.junit.Test
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val dirCandidates = listOf(
            File("src/main/res/font"),
            File("app/src/main/res/font")
        )
        val fontDir = dirCandidates.firstOrNull { it.exists() }
        val testChars = "مدیریت سرویس خودرو ۱۲۳۴۵۶۷۸۹۰ پژو گچکی"
        fontDir?.listFiles()?.sortedBy { it.name }?.forEach { f ->
            val bytes = f.readBytes()
            val buf = ByteBuffer.wrap(bytes)
            val sfntVersion = buf.int
            val numTables = buf.short.toInt() and 0xFFFF
            buf.position(12)
            val tables = mutableListOf<String>()
            for (i in 0 until numTables) {
                val tagBytes = ByteArray(4)
                buf.get(tagBytes)
                val tag = String(tagBytes)
                tables.add(tag)
                buf.int // checkSum
                buf.int // offset
                buf.int // length
            }
            val awtFont = Font.createFont(Font.TRUETYPE_FONT, f).deriveFont(24f)
            val missingChars = testChars.filter { !awtFont.canDisplay(it) }
            val img = BufferedImage(400, 60, BufferedImage.TYPE_INT_ARGB)
            val g = img.createGraphics()
            g.color = Color.WHITE
            g.fillRect(0, 0, 400, 60)
            g.color = Color.BLACK
            g.font = awtFont
            g.drawString(testChars, 10, 40)
            g.dispose()
            var blackPixels = 0L
            var hash = 0L
            for (y in 0 until 60) {
                for (x in 0 until 400) {
                    val rgb = img.getRGB(x, y)
                    if (rgb != -1) {
                        blackPixels++
                        hash = hash * 31 + (x * 100 + y)
                    }
                }
            }
            println("FONT ${f.name}: tables=$tables, missing='$missingChars', blackPixels=$blackPixels, pixelHash=$hash")
        }
        assertEquals(4, 2 + 2)
    }
}


