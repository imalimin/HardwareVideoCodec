/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <com_lmy_codec_x264_X264Encoder.h>
#include <malloc.h>
#include <string.h>
#include <sys/time.h>

//Log
#ifdef ANDROID

#include <jni.h>
#include <x264.h>
#include <libyuv.h>

#define X264_TYPE_HEADER          -0x0001  /* Headers SPS/PPS */
#define INVALID 0//未初始化
#define START 1//开始
#define STOP 1//停止
#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "JNI", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "JNI", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#endif
struct YuvFrame {
    int width;
    int height;
    uint8_t *data;
    uint8_t *y;
    uint8_t *u;
    uint8_t *v;
};
typedef struct {
    x264_param_t *param;
    x264_t *handle;
    x264_picture_t *picture;
    x264_nal_t *nal;
} Encoder;
Encoder *encoder = NULL;
struct YuvFrame yuvBuffer;
int state = INVALID;
jmethodID setTypeMethod = 0;
int hasNalHeader = 0;

static void initYuvBuffer(int width, int height) {
    int y_size = width * height;
    yuvBuffer.width = width;
    yuvBuffer.height = height;
    yuvBuffer.data = (uint8_t *) malloc(y_size * 3 / 2);
    yuvBuffer.y = yuvBuffer.data;
    yuvBuffer.u = yuvBuffer.y + y_size;
    yuvBuffer.v = yuvBuffer.u + y_size / 4;
}

static int convert(jbyte *rgb) {
    int ret = ConvertToI420((const uint8 *) rgb, yuvBuffer.width * yuvBuffer.height,
                            yuvBuffer.y, yuvBuffer.width,
                            yuvBuffer.u, yuvBuffer.width / 2,
                            yuvBuffer.v, yuvBuffer.width / 2,
                            0, 0,
                            yuvBuffer.width, yuvBuffer.height,
                            yuvBuffer.width, yuvBuffer.height,
                            kRotate0, FOURCC_ABGR);
    return ret;
}

static void setType(JNIEnv *env, jobject thiz, int type) {
    (*env)->CallVoidMethod(env, thiz, setTypeMethod, type);
}

static void initSetTypeMethod(JNIEnv *env) {
    jclass clazz = (*env)->FindClass(env, "com/lmy/codec/x264/X264Encoder");
    if (clazz == 0) {
        LOGE("com/lmy/codec/x264/X264Encoder not found");
        return;
    }

    setTypeMethod = (*env)->GetMethodID(env, clazz, "setType", "(I)V");
    if (setTypeMethod == 0) {
        LOGE("setType not found");
        return;
    }
}

static int encode_headers(jbyte *dest) {
    int nal, size = 0;
    x264_nal_t *nals;
    x264_encoder_headers(encoder->handle, &nals, &nal);
    for (int i = 0; i < nal; i++) {
        if (nals[i].i_type == NAL_SPS) {
            LOGE("SPS---------------->i=%d,len=%d", i, nals[i].i_payload);
            memcpy(dest, nals[i].p_payload, nals[i].i_payload);
            dest += nals[i].i_payload;
            size += nals[i].i_payload;
        } else if (nals[i].i_type == NAL_PPS) {
            LOGE("PPS---------------->i=%d,len=%d", i, nals[i].i_payload);
            memcpy(dest, nals[i].p_payload, nals[i].i_payload);
            dest += nals[i].i_payload;
            size += nals[i].i_payload;
        }
    }
    return size;
}

static int encode(JNIEnv *env, jobject thiz, jbyte *src, jint srcSize, jbyte *dest) {
    if (START != state) {
        LOGI("Start failed. Invalid state, encoder is not start");
        return 0;
    }
    if (0 == hasNalHeader) {
        hasNalHeader = 1;
        setType(env, thiz, X264_TYPE_HEADER);
        return encode_headers(dest);
    }

    int nNal = -1;
    x264_picture_t pic_out;
    int size = 0, i = 0;

    struct timeval start, end;
    gettimeofday(&start, NULL);
    int ret = convert(src);
    if (ret < 0) {
        LOGE("Convert failed");
        return size;
    }
    gettimeofday(&end, NULL);
    int time = end.tv_usec - start.tv_usec;
    gettimeofday(&start, NULL);

    encoder->picture->img.i_csp = X264_CSP_I420;
    encoder->picture->img.i_plane = 3;
    encoder->picture->img.plane[0] = yuvBuffer.y;
    encoder->picture->img.i_stride[0] = yuvBuffer.width;
    encoder->picture->img.plane[1] = yuvBuffer.u;
    encoder->picture->img.i_stride[1] = yuvBuffer.width / 2;
    encoder->picture->img.plane[2] = yuvBuffer.v;
    encoder->picture->img.i_stride[2] = yuvBuffer.width / 2;

//    memcpy(encoder->picture->img.plane[0], src, srcSize);
    encoder->picture->i_type = X264_TYPE_AUTO;

    if (x264_encoder_encode(encoder->handle, &(encoder->nal), &nNal, encoder->picture, &pic_out) <
        0) {
        return -1;
    }
//    LOGE("x264 frame size = %d", encoder->nal[0].i_payload)
//    createBuffer(env, thiz, size);
    for (i = 0; i < nNal; i++) {
        memcpy(dest, encoder->nal[i].p_payload, encoder->nal[i].i_payload);
        dest += encoder->nal[i].i_payload;
        size += encoder->nal[i].i_payload;
    }
    setType(env, thiz, pic_out.i_type);
    gettimeofday(&end, NULL);
    LOGI("Encode type: %d, Yuv convert time: %ld, Encode time: %ld", pic_out.i_type, time,
         (end.tv_usec - start.tv_usec));
    return size;
}

static void setVideoSize(int width, int height) {
    encoder->param->i_width = width; //set frame width
    encoder->param->i_height = height; //set frame height
}

static void setBitrate(int bitrate) {
    encoder->param->rc.i_bitrate = bitrate;
}

static void setFrameFormat(int format) {
    encoder->param->i_csp = format; // 设置输入的视频采样的格式
}

static void setFps(int fps) {
    encoder->param->i_fps_num = (uint32_t) fps;
    encoder->param->i_fps_den = 1;
}

static void setProfile(char *profile) {
    LOGI("setProfile: %s", profile);
    x264_param_apply_profile(encoder->param, profile);
}

static void setLevel(int level) {
    encoder->param->i_level_idc = level;// 11 12 13 20 for CIF;31 for 720P
}


static void fast() {
    /**
     * 设置放置 B 帧决策算法。控制 x264 如何在 P 或 B 帧之间抉择。
     * 0.关闭。总是选择 B 帧。与老的 no-b-adapt 选项相同。
     * 1. 快速算法，较快的，当 --b-frames 值较大时速度会略微加快。采用这种模式时，基本都会使用 --bframes 16。
     * 2.最优算法，较慢的，当 --b-frames 值较大时速度会大幅度降低。
     */
    encoder->param->i_bframe = 0;
    encoder->param->i_bframe_adaptive = X264_B_ADAPT_FAST;
    /**
     * 在亮度和色度两个位面进行运动预测。本选项关闭色度运动预测并能略微提高编码速度。
     */
    encoder->param->analyse.b_chroma_me = 0;
    /**
     * DCT Decimation 将去除中被认为不重要的 DCT 块。这样做可以提高编码效率， 并有些微的质量损失。
     */
    //encoder->param->analyse.b_dct_decimate = 1;
}

static void quality() {
    /**
     * 控制去块滤波器是否打开，推荐打开
     */
    encoder->param->b_deblocking_filter = 1;
    /**
     * alpha去块滤波器，取值范围 -6 ~ 6 数字越大效果越强
     */
    encoder->param->i_deblocking_filter_alphac0 = 6;
    /**
     * beta去块滤波器，取值范围 -6 ~ 6 数字越大效果越强
     */
    encoder->param->i_deblocking_filter_beta = 6;
    /**
     * Open-GOP 是一种提高压缩率的编码技术。有三种模式：
     * none: 关闭
     * normal: 启用
     * bluray: 启用。一个较低效率版本的 Open-GOP，当压制蓝光时 normal 模式不能工作。
     * 一些解码器不完全支持Open-GOP 流，这就是为什么默认是关闭的。你需要测试播放视频流的解码器，
     * 或者等到Open-GOP 被普遍支持。
     */
    encoder->param->b_open_gop = 1;
    /**
     * weightp=2
     * 在 P 帧中开启加权预测用于提高压缩率。同时提高淡入淡出场景质量。值越大越慢。
     */
    encoder->param->analyse.i_weighted_pred = X264_WEIGHTP_SMART;
    encoder->param->analyse.b_weighted_bipred = X264_WEIGHTP_SMART;
}

static void init(JNIEnv *env, char *preset, char *tune) {
    initSetTypeMethod(env);
    encoder = (Encoder *) malloc(sizeof(Encoder));
    encoder->param = (x264_param_t *) malloc(sizeof(x264_param_t));
    encoder->picture = (x264_param_t *) malloc(sizeof(x264_picture_t));
    //开启多帧并行编码
    encoder->param->b_sliced_threads = 0;
    encoder->param->i_threads = 4;
    /**
     * 是否复制sps和pps放在每个关键帧的前面
     */
    encoder->param->b_repeat_headers = 0;
    /**
     * 恒定质量
     * ABR（平均码率）/CQP（恒定质量）/CRF（恒定码率）
     * ABR模式下调整i_bitrate
     * CQP下调整i_qp_constant调整QP值，太细致了人眼也分辨不出来，为了增加编码速度降低数据量还是设大些好
     * CRF下调整f_rf_constant和f_rf_constant_max影响编码速度和图像质量（数据量），码率和图像效果参数失效
     */
    encoder->param->rc.i_rc_method = X264_RC_CQP;
    /**
     * 范围0~51，值越大图像越模糊，默认23
     */
    //encoder->param->rc.i_qp_constant = 51;
    /**
     * inter，取值范围1~32
     * 值越大数据量相应越少，占用带宽越低
     */
    encoder->param->analyse.i_luma_deadzone[0] = 32;
    /**
     * intra，取值范围1~32
     * 值越大数据量相应越少，占用带宽越低
     */
    encoder->param->analyse.i_luma_deadzone[1] = 32;
    /**
     * 快速P帧跳过检测
     */
    encoder->param->analyse.b_fast_pskip = 1;
    /**
     * 是否允许非确定性时线程优化
     */
    encoder->param->b_deterministic = 0;
    /**
     * 强制采用典型行为，而不是采用独立于cpu的优化算法
     */
    encoder->param->b_cpu_independent = 0;
    fast();
    quality();
    x264_param_default_preset(encoder->param, preset, tune);
}

static void start() {
    if (INVALID != state) {
        LOGI("Start failed. Invalid state, encoder is not invalid");
        return;
    }
    state = START;
    initYuvBuffer(encoder->param->i_width, encoder->param->i_height);
    if ((encoder->handle = x264_encoder_open(encoder->param)) == 0) {
        return;
    }
    /* Create a new pic */
    x264_picture_alloc(encoder->picture, encoder->param->i_csp, encoder->param->i_width,
                       encoder->param->i_height);
    LOGI("X264Encoder start");
}

static void stop() {
    if (START != state) {
        LOGI("Stop failed. Invalid state, encoder is not start");
        return;
    }
    state = STOP;
    if (encoder->picture) {
        x264_picture_clean(encoder->picture);
        free(encoder->picture);
        encoder->picture = 0;
    }
    if (encoder->param) {
        free(encoder->param);
        encoder->param = 0;
    }
    if (encoder->handle) {
        x264_encoder_close(encoder->handle);
    }
    free(encoder);
    LOGI("X264Encoder stop");
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_init
        (JNIEnv *env, jobject thiz, jstring preset, jstring tune) {
    char *presetTmp = (char *) (*env)->GetStringUTFChars(env, preset, NULL);
    char *tuneTmp = (char *) (*env)->GetStringUTFChars(env, tune, NULL);
    init(env, presetTmp, tuneTmp);
    (*env)->ReleaseStringUTFChars(env, preset, presetTmp);
    (*env)->ReleaseStringUTFChars(env, tune, tuneTmp);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_start
        (JNIEnv *env, jobject thiz) {
    start();
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_stop
        (JNIEnv *env, jobject thiz) {
    stop();
}

JNIEXPORT jint JNICALL Java_com_lmy_codec_x264_X264Encoder_encode
        (JNIEnv *env, jobject thiz, jbyteArray src, jint srcSize, jbyteArray out) {
    jbyte *srcBuffer = (*env)->GetByteArrayElements(env, src, 0);
    jbyte *destBuffer = (*env)->GetByteArrayElements(env, out, 0);
    int size = encode(env, thiz, srcBuffer, srcSize, destBuffer);
    (*env)->ReleaseByteArrayElements(env, src, srcBuffer, 0);
    (*env)->ReleaseByteArrayElements(env, out, destBuffer, 0);
    return size;
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setVideoSize
        (JNIEnv *env, jobject thiz, jint width, jint height) {
    setVideoSize(width, height);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setBitrate
        (JNIEnv *env, jobject thiz, jint bitrate) {
    setBitrate(bitrate);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setFrameFormat
        (JNIEnv *env, jobject thiz, jint format) {
    setFrameFormat(format);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setFps
        (JNIEnv *env, jobject thiz, jint fps) {
    setFps(fps);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setProfile
        (JNIEnv *env, jobject thiz, jstring profile) {
    char *profileTmp = (char *) (*env)->GetStringUTFChars(env, profile, NULL);
    setProfile(profileTmp);
    (*env)->ReleaseStringUTFChars(env, profile, profileTmp);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setLevel
        (JNIEnv *env, jobject thiz, jint level) {
    setLevel(level);
}