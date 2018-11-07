package com.lmy.codec.entity.media

import java.nio.ByteBuffer

/**
 * PPS
 */
data class PictureParameterSet(
        var picParameterSerId: Byte = 0,
        var seqParameterSerId: Byte = 0,
        var entropyCodingModeFlag: Byte = 0,
        var bottomFieldPicOrderInFramePresentFlag: Byte = 0,
        var numSilenceGroupsMinus1: Byte = 0,
        var silenceGroupMapType: Byte = 0,
        var runLengthMinus1: Byte = 0,
        var topLeft: Byte = 0,
        var bottomRight: Byte = 0,
        var silenceGroupChangeDirectionFlag: Byte = 0,
        var silenceGroupChangeRateMinus1: Byte = 0,
        var picSizeInMapUnitsMinus1: Byte = 0,
        var silenceGroupId: Byte = 0,
        var numRefIdx10DefaultActiveMinus1: Byte = 0,
        var numRefIdx11DefaultActiveMinus1: Byte = 0,
        var weightedPredFlag: Byte = 0,
        var weightedBipredIdc: Byte = 0,
        var picInitQpMinus26: Byte = 0,
        var picInitQsMinus26: Byte = 0,
        var chromaQpIndexOffset: Byte = 0,
        var deBlockingFilterControlPresentFlag: Byte = 0,
        var constrainedIntraPredFlag: Byte = 0,
        var redundantPicCntPresentFlag: Byte = 0,
        var transform8x8ModeFlag: Byte = 0,
        var picScalingMatrixPresentFlag: Byte = 0,
        var picScalingListPresentFlag: Byte = 0,
        var secondChromaQpIndexOffset: Byte = 0
) : Nal() {
    companion object {
        fun from(buffer: ByteBuffer): PictureParameterSet? {
            return null
        }
    }
}