package com.lmy.sample

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.lmy.codec.presenter.FilterSupport
import com.lmy.codec.texture.impl.filter.*

/**
 * Created by lmyooyo@gmail.com on 2018/7/24.
 */
class FilterController(private val mVideoRecorder: FilterSupport,
                       private val progressLayout: ViewGroup)
    : SeekBar.OnSeekBarChangeListener {
    companion object {
        private val FILTERS = arrayOf(
                "Normal", "Beauty", "Beauty V4", "Nature ", "Clean ",
                "Vivid ", "Beach", "Brannan", "Coral", "Crisp",
                "Fresh", "Grass", "Inkwell", "Lolita", "Pink",
                "Sunset", "Sweet", "Urban", "Valencia", "Vintage",
                "Walden", "Pixar", "Chromatic", "Grey", "Pixelation",
                "Hue", "Gamma", "Brightness", "Sepia", "Sharpness",
                "Saturation", "Exposure", "Highlight Shadow", "Monochrome", "White Balance",
                "Vignette", "Crosshatch", "Smooth", "Sketch", "Halftone",
                "Haze")
    }

    private var oneBar: SeekBar = progressLayout.getChildAt(0) as SeekBar
    private var twoBar: SeekBar = progressLayout.getChildAt(1) as SeekBar
    private var thBar: SeekBar = progressLayout.getChildAt(2) as SeekBar
    private var fBar: SeekBar = progressLayout.getChildAt(3) as SeekBar

    init {
        oneBar.setOnSeekBarChangeListener(this)
        twoBar.setOnSeekBarChangeListener(this)
        thBar.setOnSeekBarChangeListener(this)
        fBar.setOnSeekBarChangeListener(this)
    }

    fun chooseFilter(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("EFFECT")
            setItems(FILTERS) { dialog, which ->
                choose(which)
                dialog.dismiss()
            }
        }.show()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        mVideoRecorder.getFilter()?.setValue(progressLayout.indexOfChild(seekBar), progress)
    }

    private fun show(count: Int) {
        oneBar.visibility = if (count > 0) View.VISIBLE else View.GONE
        twoBar.visibility = if (count > 1) View.VISIBLE else View.GONE
        thBar.visibility = if (count > 2) View.VISIBLE else View.GONE
        fBar.visibility = if (count > 3) View.VISIBLE else View.GONE
    }

    private fun choose(which: Int) {
        when (which) {
            0 -> {
                mVideoRecorder.setFilter(NormalFilter::class.java)
                show(0)
            }
            1 -> {
                mVideoRecorder.setFilter(BeautyFilter::class.java)
                show(3)
                oneBar.progress = 55
                twoBar.progress = 25
                thBar.progress = 15
            }
            2 -> {
                mVideoRecorder.setFilter(BeautyV4Filter::class.java)
                show(3)
                oneBar.progress = 50
                twoBar.progress = 70
                thBar.progress = 7
            }
            3 -> {
                mVideoRecorder.setFilter(NatureFilter::class.java)
                show(0)
            }
            4 -> {
                mVideoRecorder.setFilter(CleanFilter::class.java)
                show(0)
            }
            5 -> {
                mVideoRecorder.setFilter(VividFilter::class.java)
                show(0)
            }
            6 -> {
                mVideoRecorder.setFilter(BeachFilter::class.java)
                show(0)
            }
            7 -> {
                mVideoRecorder.setFilter(BrannanFilter::class.java)
                show(0)
            }
            8 -> {
                mVideoRecorder.setFilter(CoralFilter::class.java)
                show(0)
            }
            9 -> {
                mVideoRecorder.setFilter(CrispFilter::class.java)
                show(0)
            }
            10 -> {
                mVideoRecorder.setFilter(FreshFilter::class.java)
                show(0)
            }
            11 -> {
                mVideoRecorder.setFilter(GrassFilter::class.java)
                show(0)
            }
            12 -> {
                mVideoRecorder.setFilter(InkwellFilter::class.java)
                show(0)
            }
            13 -> {
                mVideoRecorder.setFilter(LolitaFilter::class.java)
                show(0)
            }
            14 -> {
                mVideoRecorder.setFilter(PinkFilter::class.java)
                show(0)
            }
            15 -> {
                mVideoRecorder.setFilter(SunsetFilter::class.java)
                show(0)
            }
            16 -> {
                mVideoRecorder.setFilter(SweetFilter::class.java)
                show(0)
            }
            17 -> {
                mVideoRecorder.setFilter(UrbanFilter::class.java)
                show(0)
            }
            18 -> {
                mVideoRecorder.setFilter(ValenciaFilter::class.java)
                show(0)
            }
            19 -> {
                mVideoRecorder.setFilter(VintageFilter::class.java)
                show(0)
            }
            20 -> {
                mVideoRecorder.setFilter(WaldenFilter::class.java)
                show(0)
            }
            21 -> {
                mVideoRecorder.setFilter(PixarFilter::class.java)
                show(0)
            }
            22 -> {
                mVideoRecorder.setFilter(ChromaticFilter::class.java)
                show(1)
                oneBar.progress = 10
            }
            23 -> {
                mVideoRecorder.setFilter(GreyFilter::class.java)
                show(0)
            }
            24 -> {
                mVideoRecorder.setFilter(PixelationFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            25 -> {
                mVideoRecorder.setFilter(HueFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            26 -> {
                mVideoRecorder.setFilter(GammaFilter::class.java)
                show(1)
                oneBar.progress = 33
            }
            27 -> {
                mVideoRecorder.setFilter(BrightnessFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            28 -> {
                mVideoRecorder.setFilter(SepiaFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            29 -> {
                mVideoRecorder.setFilter(SharpnessFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            30 -> {
                mVideoRecorder.setFilter(SaturationFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            31 -> {
                mVideoRecorder.setFilter(ExposureFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            32 -> {
                mVideoRecorder.setFilter(HighlightShadowFilter::class.java)
                show(2)
                oneBar.progress = 0
                twoBar.progress = 0
            }
            33 -> {
                mVideoRecorder.setFilter(MonochromeFilter::class.java)
                show(4)
                oneBar.progress = 0
                twoBar.progress = 60
                thBar.progress = 45
                fBar.progress = 30
            }
            34 -> {
                mVideoRecorder.setFilter(WhiteBalanceFilter::class.java)
                show(2)
                oneBar.progress = 50
                twoBar.progress = 0
            }
            35 -> {
                mVideoRecorder.setFilter(VignetteFilter::class.java)
                show(4)
                oneBar.progress = 50
                twoBar.progress = 50
                thBar.progress = 50
                fBar.progress = 100
            }
            36 -> {
                mVideoRecorder.setFilter(CrosshatchFilter::class.java)
                show(2)
                oneBar.progress = 30
                twoBar.progress = 30
            }
            37 -> {
                mVideoRecorder.setFilter(SmoothFilter::class.java)
                show(1)
                oneBar.progress = 30
            }
            38 -> {
                mVideoRecorder.setFilter(SketchFilter::class.java)
                show(1)
                oneBar.progress = 30
            }
            39 -> {
                mVideoRecorder.setFilter(HalftoneFilter::class.java)
                show(2)
                oneBar.progress = 30
                twoBar.progress = 10
            }
            40 -> {
                mVideoRecorder.setFilter(HazeFilter::class.java)
                show(2)
                oneBar.progress = 50
                twoBar.progress = 50
            }
            else -> {
                mVideoRecorder.setFilter(NormalFilter::class.java)
                show(0)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }
}