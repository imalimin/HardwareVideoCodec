//
// Created by nonolive66 on 2018/4/3.
//

#include <com_lmy_codec_x264_X264Encoder.h>

//Log
#ifdef ANDROID

#include <jni.h>
#include <x264.h>

#define INVALID 0//未初始化
#define START 1//开始
#define STOP 1//停止
#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "JNI", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "JNI", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#endif
typedef struct {
    x264_param_t *param;
    x264_t *handle;
    x264_picture_t *picture;
    x264_nal_t *nal;
} Encoder;
Encoder *encoder = NULL;
int state = INVALID;

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_init
        (JNIEnv *env, jobject thiz) {
    encoder = (Encoder *) malloc(sizeof(Encoder));
    encoder->param = (x264_param_t *) malloc(sizeof(x264_param_t));
    encoder->picture = (x264_param_t *) malloc(sizeof(x264_picture_t));
    //开启多帧并行编码
    encoder->param->b_sliced_threads = 0;
    //encoder->param->i_threads = 8;
    x264_param_default(encoder->param);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_start
        (JNIEnv *env, jobject thiz) {
    if (INVALID != state) {
        LOGI("Start failed. Invalid state");
        return;
    }

    if ((encoder->handle = x264_encoder_open(encoder->param)) == 0) {
        return;
    }
    /* Create a new pic */
    x264_picture_alloc(encoder->picture, encoder->param->i_csp, encoder->param->i_width,
                       encoder->param->i_height);
    LOGI("X264Encoder start");
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_stop
        (JNIEnv *env, jobject thiz) {
    if (START != state) {
        LOGI("Stop failed. Invalid state");
        return;
    }
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

JNIEXPORT jint JNICALL Java_com_lmy_codec_x264_X264Encoder_encode
        (JNIEnv *env, jobject thiz, jbyteArray src, jint srcSize, jbyteArray out) {
    jbyte *Buf = (*env)->GetByteArrayElements(env, src, 0);
    jbyte *h264Buf = (*env)->GetByteArrayElements(env, out, 0);

    x264_picture_t pic_out;
    int i_data = 0;
    int nNal = -1;
    int result = 0;
    int i = 0, j = 0;
    int nPix = 0;

    unsigned char *pTmpOut = h264Buf;

    int nPicSize = encoder->param->i_width * encoder->param->i_height * 3;
    memcpy(encoder->picture->img.plane[0], Buf, nPicSize);

    encoder->picture->i_type = X264_TYPE_AUTO;

    if (x264_encoder_encode(encoder->handle, &(encoder->nal), &nNal, encoder->picture, &pic_out) <
        0) {
        return -1;
    }
    for (i = 0; i < nNal; i++) {
        memcpy(pTmpOut, encoder->nal[i].p_payload, encoder->nal[i].i_payload);
        pTmpOut += encoder->nal[i].i_payload;
        result += encoder->nal[i].i_payload;
    }

    (*env)->ReleaseByteArrayElements(env, src, Buf, 0);
    (*env)->ReleaseByteArrayElements(env, out, h264Buf, 0);

    return result;
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setVideoSize
        (JNIEnv *env, jobject thiz, jint width, jint height) {
    encoder->param->i_width = width; //set frame width
    encoder->param->i_height = height; //set frame height
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setBitrate
        (JNIEnv *env, jobject thiz, jint bitrate) {
    encoder->param->rc.i_bitrate = bitrate;
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setFrameFormat
        (JNIEnv *env, jobject thiz, jint format) {
    encoder->param->i_csp = format; // 设置输入的视频采样的格式
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setFps
        (JNIEnv *env, jobject thiz, jint fps) {
    encoder->param->i_fps_num = (uint32_t) fps;
    encoder->param->i_fps_den = 1;
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setProfile
        (JNIEnv *env, jobject thiz, jstring profile) {
    char *profileTmp = (char *) (*env)->GetStringUTFChars(env, profile, NULL);
    LOGI("setProfile: %s", profileTmp);
    x264_param_apply_profile(encoder->param, profileTmp);

    (*env)->ReleaseStringUTFChars(env, profile, profileTmp);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x264_X264Encoder_setLevel
        (JNIEnv *env, jobject thiz, jint level) {
    encoder->param->i_level_idc = level;// 11 12 13 20 for CIF;31 for 720P
}