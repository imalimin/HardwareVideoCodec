package com.lmy.sample

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.lmy.codec.presenter.VideoRecorder
import com.lmy.codec.texture.impl.filter.*

/**
 * Created by lmyooyo@gmail.com on 2018/7/24.
 */
class FilterController(private val mVideoRecorder: VideoRecorder,
                       private val progressLayout: ViewGroup)
    : SeekBar.OnSeekBarChangeListener {
    companion object {
        private val FILTERS = arrayOf("Normal", "Beauty", "Beauty V4", "Grey", "Pixelation", "Hue",
                "Gamma", "Brightness", "Sepia", "Sharpness", "Saturation",
                "Exposure", "Highlight Shadow", "Monochrome", "White Balance", "Vignette",
                "Crosshatch", "Smooth", "Sketch", "Halftone", "Haze")
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
                mVideoRecorder.setFilter(GreyFilter::class.java)
                show(0)
            }
            4 -> {
                mVideoRecorder.setFilter(PixelationFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            5 -> {
                mVideoRecorder.setFilter(HueFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            6 -> {
                mVideoRecorder.setFilter(GammaFilter::class.java)
                show(1)
                oneBar.progress = 33
            }
            7 -> {
                mVideoRecorder.setFilter(BrightnessFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            8 -> {
                mVideoRecorder.setFilter(SepiaFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            9 -> {
                mVideoRecorder.setFilter(SharpnessFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            10 -> {
                mVideoRecorder.setFilter(SaturationFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            11 -> {
                mVideoRecorder.setFilter(ExposureFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            12 -> {
                mVideoRecorder.setFilter(HighlightShadowFilter::class.java)
                show(2)
                oneBar.progress = 0
                twoBar.progress = 0
            }
            13 -> {
                mVideoRecorder.setFilter(MonochromeFilter::class.java)
                show(4)
                oneBar.progress = 0
                twoBar.progress = 60
                thBar.progress = 45
                fBar.progress = 30
            }
            14 -> {
                mVideoRecorder.setFilter(WhiteBalanceFilter::class.java)
                show(2)
                oneBar.progress = 50
                twoBar.progress = 0
            }
            15 -> {
                mVideoRecorder.setFilter(VignetteFilter::class.java)
                show(4)
                oneBar.progress = 50
                twoBar.progress = 50
                thBar.progress = 50
                fBar.progress = 100
            }
            16 -> {
                mVideoRecorder.setFilter(CrosshatchFilter::class.java)
                show(2)
                oneBar.progress = 30
                twoBar.progress = 30
            }
            17 -> {
                mVideoRecorder.setFilter(SmoothFilter::class.java)
                show(1)
                oneBar.progress = 30
            }
            18 -> {
                mVideoRecorder.setFilter(SketchFilter::class.java)
                show(1)
                oneBar.progress = 30
            }
            19 -> {
                mVideoRecorder.setFilter(HalftoneFilter::class.java)
                show(2)
                oneBar.progress = 30
                twoBar.progress = 10
            }
            20 -> {
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