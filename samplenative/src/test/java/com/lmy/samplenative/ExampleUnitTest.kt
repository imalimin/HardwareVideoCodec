package com.lmy.samplenative

import com.lmy.hwvcnative.filter.PinkFilter
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        System.loadLibrary("hwvcom")
        System.loadLibrary("hwvc_render")
        System.loadLibrary("hwvc_native")
        PinkFilter(arrayOf("sTexture2", "sTexture3"),
                arrayOf("textures/pink.png", "textures/pink.png"))
    }

    @Test
    fun testPNG2CDATA() {
        val file = File("D:/clean.png")
        if (file.exists()) {
            val input = FileInputStream(file)
            val output = FileOutputStream("${file.absolutePath}.xml")
            output.write("<![CDATA[".toByteArray(Charset.forName("utf-8")))

            val buf = ByteArray(input.available())
            input.read(buf)
            output.write(Base64.getEncoder().encode(buf))

            output.write("]]>".toByteArray(Charset.forName("utf-8")))
            input.close()
            output.close()
            return
        }
        throw IOException("File not found")
    }
}
