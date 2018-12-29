package com.lmy.samplenative

import com.lmy.hwvcnative.filter.PinkFilter
import org.junit.Test

import org.junit.Assert.*

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
}
