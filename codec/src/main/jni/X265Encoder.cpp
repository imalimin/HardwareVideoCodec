//
// Created by nonolive66 on 2018/6/13.
//

#include "X265Encoder.h"

static int state = INVALID;
static int width, height, bitrate, fps, y_size;
static int format;
static x265_param *pParam = NULL;
static x265_encoder *pHandle = NULL;
static x265_picture *pPic_in = NULL;
static x265_nal *pNals = NULL;
static uint32_t iNal = 0;
static bool hasNalHeader = false;
static int destSize = 0;

void X265Encoder::reset() {
    state = INVALID;
    free(pHandle);
    pHandle = NULL;
    free(pNals);
    pNals = NULL;
}

bool X265Encoder::encodeHeader(char *dest, int *size, int *type) {
    x265_encoder_headers(pHandle, &pNals, &iNal);
    for (int i = 0; i < iNal; i++) {
        if (pNals[i].type == NAL_UNIT_SPS) {
            LOGE("SPS---------------->i=%d,len=%d", i, pNals[i].sizeBytes);
            memcpy(dest, pNals[i].payload, pNals[i].sizeBytes);
            dest += pNals[i].sizeBytes;
            destSize += pNals[i].sizeBytes;
        } else if (pNals[i].type == NAL_UNIT_PPS) {
            LOGE("PPS---------------->i=%d,len=%d", i, pNals[i].sizeBytes);
            memcpy(dest, pNals[i].payload, pNals[i].sizeBytes);
            dest += pNals[i].sizeBytes;
            destSize += pNals[i].sizeBytes;
        }
    }
    size[0] = destSize;
    type[0] = X265_TYPE_HEADER;
    return true;
}

bool X265Encoder::fillSrc(char *argb) {
    int ret = libyuv::ConvertToI420((const uint8 *) argb, width * height,
                                    (uint8 *) pPic_in->planes[0], width,
                                    (uint8 *) pPic_in->planes[1], width / 2,
                                    (uint8 *) pPic_in->planes[2], width / 2,
                                    0, 0,
                                    width, height,
                                    width, height,
                                    libyuv::kRotate0, libyuv::FOURCC_ABGR);
    return ret > 0;
}

bool X265Encoder::encode(char *src, char *dest, int *size, int *type) {
    if (START != state) {
        LOGI("Start failed. Invalid state, encoder is not start");
        return 0;
    }
    if (hasNalHeader) {
        hasNalHeader = true;
        return encodeHeader(dest, size, type);
    }
    if (fillSrc(src)) {
        LOGE("Convert failed");
        return false;
    }
    int ret = x265_encoder_encode(pHandle, &pNals, &iNal, NULL, NULL);
    if (0 == ret) return false;
    for (int i = 0; i < iNal; i++) {
        memcpy(dest, pNals[i].payload, pNals[i].sizeBytes);
        dest += pNals[i].sizeBytes;
        destSize += pNals[i].sizeBytes;
    }
    size[0] = destSize;
    type[0] = pNals->type;
    return true;
}

bool X265Encoder::start() {
    reset();
    if (INVALID != state) {
        LOGI("Start failed. Invalid state, encoder is not invalid");
        return false;
    }
    state = START;
    LOGE("start");
    pParam = x265_param_alloc();
    x265_param_default(pParam);
    pParam->bRepeatHeaders = 0;//write sps,pps before keyframe
    pParam->internalCsp = format;
    pParam->sourceWidth = width;
    pParam->sourceHeight = height;
    pParam->fpsNum = fps;
    pParam->fpsDenom = 1;
    pHandle = x265_encoder_open(pParam);
    if (pHandle == NULL) {
        LOGE("x265_encoder_open err");
        reset();
        return false;
    }
    y_size = pParam->sourceWidth * pParam->sourceHeight;
    pPic_in = x265_picture_alloc();
    x265_picture_init(pParam, pPic_in);
    char *buff = NULL;
    switch (pParam->internalCsp) {
        case X265_CSP_I444: {
            buff = (char *) malloc(y_size * 3);
            pPic_in->planes[0] = buff;//Y
            pPic_in->planes[1] = buff + y_size;//U
            pPic_in->planes[2] = buff + y_size * 2;//V
            pPic_in->stride[0] = width;
            pPic_in->stride[1] = width;
            pPic_in->stride[2] = width;
            break;
        }
        case X265_CSP_I420: {
            buff = (char *) malloc(y_size * 3 / 2);
            pPic_in->planes[0] = buff;//Y
            pPic_in->planes[1] = buff + y_size;//U
            pPic_in->planes[2] = buff + y_size * 5 / 4;//V
            pPic_in->stride[0] = width;
            pPic_in->stride[1] = width / 2;
            pPic_in->stride[2] = width / 2;
            break;
        }
        default: {
            LOGE("Colorspace Not Support.");
            reset();
            return false;
        }
    }
    return true;
}

bool X265Encoder::flush(char *dest, int *size, int *type) {
    int ret = x265_encoder_encode(pHandle, &pNals, &iNal, NULL, NULL);
    if (ret == 0)
        return false;
    for (int i = 0; i < iNal; i++) {
        memcpy(dest, pNals[i].payload, pNals[i].sizeBytes);
        dest += pNals[i].sizeBytes;
        destSize += pNals[i].sizeBytes;
    }
    size[0] = destSize;
    type[0] = pNals->type;
    return true;
}

void X265Encoder::stop() {
    if (START != state) {
        LOGI("Stop failed. Invalid state, encoder is not start");
        return;
    }
    state = STOP;
    LOGE("stop");
    x265_encoder_close(pHandle);
    x265_picture_free(pPic_in);
    if (NULL != pPic_in)
        free(pPic_in->planes[0]);
    x265_param_free(pParam);
}

X265Encoder::X265Encoder() {
}

X265Encoder::~X265Encoder() {
    reset();
}

void X265Encoder::setVideoSize(int w, int h) {
    width = w;
    height = h;
}

void X265Encoder::setBitrate(int b) {
    bitrate = b;
}

void X265Encoder::setFrameFormat(int f) {
    format = f;
}

void X265Encoder::setFps(int f) {
    fps = f;
}
