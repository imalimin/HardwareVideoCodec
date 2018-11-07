package com.lmy.codec.entity.media

import com.lmy.codec.util.debug_e
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * SPS
 * {@link https://www.cnblogs.com/wainiwann/p/7477794.html}
 */
data class SequenceParameterSet(
        var profileIdc: Byte = 0,//8 bit,profile
        var constraintSet0Flag: Byte = 0,//1 bit
        var constraintSet1Flag: Byte = 0,//1 bit
        var constraintSet2Flag: Byte = 0,//1 bit
        var constraintSet3Flag: Byte = 0,//1 bit
        var constraintSet4Flag: Byte = 0,//1 bit
        var constraintSet5Flag: Byte = 0,//1 bit
        var reservedZero2Bits: Byte = 0,//2 bit
        var levelIdc: Byte = 0,//8 bit,level
        var seqParameterSetId: Byte = 0,//1 bit,当前的序列参数集的id
        var chromaFormatIdc: Byte = 0,
        var separateColourPlaneFlag: Byte = 0,
        var bitDepthLumaMinus8: Byte = 0,
        var bitDepthChromaMinus8: Byte = 0,
        var qpPrimeYZeroTransformBypassFlag: Byte = 0,
        var seqScalingMatrixPresentFlag: Byte = 0,
//        var seqScalingListPresentFlag: Byte = 0,
        var log2MaxFrameNumMinus4: Byte = 0,//用于计算MaxFrameNum的值
        var picOrderCntType: Byte = 0,
        var log2MaxPicOrderCntLsbMinus4: Byte = 0,
        var deltaPicOrderAlwaysZeroFlag: Byte = 0,
        var offsetForNonRefPic: Byte = 0,
        var offsetForTopToBottomField: Byte = 0,
        var numRefFramesInPicOrderCntCycle: Byte = 0,
//        var offsetForRefFrame: Byte = 0,
        var maxNumRefFrames: Byte = 0,//参考帧的最大数目
        var gapsInFrameNumValueAllowedFlag: Byte = 0,
        var picWidthInMbsMinus1: Byte = 0,//用于计算图像的宽度,frame_width = 16 × (pic_width_in_mbs_minus1 + 1);
        var picHeightInMbsMinus1: Byte = 0,//用于计算图像的高度
        var frameMbsOnlyFlag: Byte = 0,
        var mbAdaptiveFrameFieldFlag: Byte = 0,
        var direct8x8InferenceFlag: Byte = 0,
        var frameCroppingFlag: Byte = 0,
        var frameCroppingRectLeftOffset: Byte = 0,
        var frameCroppingRectRightOffset: Byte = 0,
        var frameCroppingRectTopOffset: Byte = 0,
        var frameCroppingRectBottomOffset: Byte = 0,
        var vuiParametersPresentFlag: Byte = 0
) : Nal() {
    companion object {
        fun from(buffer: ByteBuffer): SequenceParameterSet? {
            if ((0 == buffer.get(0).toInt() && 0 == buffer.get(1).toInt() && 1 == buffer.get(2).toInt())) {//0x00 0x00 0x01
                if (7 != (buffer.get(3) and 0x1f).toInt()) {
                    buffer.rewind()
                    return null
                }
                buffer.position(4)
            } else if (0 == buffer.get(0).toInt() && 0 == buffer.get(1).toInt()
                    && 0 == buffer.get(2).toInt() && 1 == buffer.get(3).toInt()) {//0x00 0x00 0x00 0x01
                if (7 != (buffer.get(4) and 0x1f).toInt()) {
                    buffer.rewind()
                    return null
                }
                buffer.position(5)
            }
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            buffer.rewind()
            debug_e("size: ${data.size}")
            return SequenceParameterSet().apply {
                profileIdc = data[0]
            }
        }
    }
}