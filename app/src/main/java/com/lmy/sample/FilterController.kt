package com.lmy.sample

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.lmy.codec.RecordPresenter
import com.lmy.codec.texture.impl.filter.*

/**
 * Created by lmyooyo@gmail.com on 2018/7/24.
 */
class FilterController(private val mPresenter: RecordPresenter,
                       private val progressLayout: ViewGroup)
    : SeekBar.OnSeekBarChangeListener {
    companion object {
        private val FILTERS = arrayOf("Normal", "Grey", "Beauty", "Pixelation", "Hue",
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
        mPresenter.getFilter()?.setValue(progressLayout.indexOfChild(seekBar), progress)
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
                mPresenter.setFilter(NormalFilter::class.java)
                show(0)
            }
            1 -> {
                mPresenter.setFilter(GreyFilter::class.java)
                show(0)
            }
            2 -> {
                mPresenter.setFilter(BeautyFilter::class.java)
                show(4)
                oneBar.progress = 0
                twoBar.progress = 50
                thBar.progress = 0
                fBar.progress = 50
            }
            3 -> {
                mPresenter.setFilter(PixelationFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            4 -> {
                mPresenter.setFilter(HueFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            5 -> {
                mPresenter.setFilter(GammaFilter::class.java)
                show(1)
                oneBar.progress = 33
            }
            6 -> {
                mPresenter.setFilter(BrightnessFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            7 -> {
                mPresenter.setFilter(SepiaFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            8 -> {
                mPresenter.setFilter(SharpnessFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            9 -> {
                mPresenter.setFilter(SaturationFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            10 -> {
                mPresenter.setFilter(ExposureFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            11 -> {
                mPresenter.setFilter(HighlightShadowFilter::class.java)
                show(2)
                oneBar.progress = 0
                twoBar.progress = 0
            }
            12 -> {
                mPresenter.setFilter(MonochromeFilter::class.java)
                show(4)
                oneBar.progress = 0
                twoBar.progress = 60
                thBar.progress = 45
                fBar.progress = 30
            }
            13 -> {
                mPresenter.setFilter(WhiteBalanceFilter::class.java)
                show(2)
                oneBar.progress = 50
                twoBar.progress = 0
            }
            14 -> {
                mPresenter.setFilter(VignetteFilter::class.java)
                show(4)
                oneBar.progress = 50
                twoBar.progress = 50
                thBar.progress = 50
                fBar.progress = 100
            }
            15 -> {
                mPresenter.setFilter(CrosshatchFilter::class.java)
                show(2)
                oneBar.progress = 30
                twoBar.progress = 30
            }
            16 -> {
                mPresenter.setFilter(SmoothFilter::class.java)
                show(1)
                oneBar.progress = 30
            }
            17 -> {
                mPresenter.setFilter(SketchFilter::class.java)
                show(1)
                oneBar.progress = 30
            }
            18 -> {
                mPresenter.setFilter(HalftoneFilter::class.java)
                show(2)
                oneBar.progress = 30
                twoBar.progress = 10
            }
            19 -> {
                mPresenter.setFilter(HazeFilter::class.java)
                show(2)
                oneBar.progress = 50
                twoBar.progress = 50
            }
            else -> {
                mPresenter.setFilter(NormalFilter::class.java)
                show(0)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }
}