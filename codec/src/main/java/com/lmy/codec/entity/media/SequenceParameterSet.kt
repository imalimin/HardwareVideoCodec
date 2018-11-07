package com.lmy.codec.entity.media

import java.nio.ByteBuffer

/**
 * SPS
 * @link https://www.cnblogs.com/wainiwann/p/7477794.html
 */
data class SequenceParameterSet(
        var profileIdc: Byte = 0,//8 bit
        var constraintSet0Flag: Byte = 0,//1 bit
        var constraintSet1Flag: Byte = 0,//1 bit
        var constraintSet2Flag: Byte = 0,//1 bit
        var constraintSet3Flag: Byte = 0,//1 bit
        var constraintSet4Flag: Byte = 0,//1 bit
        var constraintSet5Flag: Byte = 0,//1 bit
        var reservedZero2Bits: Byte = 0,//2 bit
        var levelIdc: Byte = 0,//8 bit
        var seqParameterSetId: Byte = 0,//1 bit
        var chromaFormatIdc: Byte = 0,
        var separateColourPlaneFlag: Byte = 0,
        var bitDepthLumaMinus8: Byte = 0,
        var bitDepthChromaMinus8: Byte = 0,
        var qpPrimeYZeroTransformBypassFlag: Byte = 0,
        var seqScalingMatrixPresentFlag: Byte = 0,
//        var seqScalingListPresentFlag: Byte = 0,
        var log2MaxFrameNumMinus4: Byte = 0,
        var picOrderCntType: Byte = 0,
        var log2MaxPicOrderCntLsbMinus4: Byte = 0,
        var deltaPicOrderAlwaysZeroFlag: Byte = 0,
        var offsetForNonRefPic: Byte = 0,
        var offsetForTopToBottomField: Byte = 0,
        var numRefFramesInPicOrderCntCycle: Byte = 0,
//        var offsetForRefFrame: Byte = 0,
        var maxNumRefFrames: Byte = 0,
        var gapsInFrameNumValueAllowedFlag: Byte = 0,
        var picWidthInMbsMinus1: Byte = 0,
        var picHeightInMbsMinus1: Byte = 0,
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
            return null
        }
    }
}