//
// Created by limin on 2018/6/13.
//

#include "X264Encoder.h"

typedef struct {
    x264_param_t *param;
    x264_t *handle;
    x264_picture_t *picture;
    x264_nal_t *nal;
} Encoder;
static Encoder *encoder = NULL;
static int state = INVALID;
static bool hasNalHeader = false;

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
    /**
     * 运动估计算法
     */
    encoder->param->analyse.i_me_method = X264_ME_UMH;
    encoder->param->analyse.i_me_range = 16;
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

static void config() {
    x264_param_default_preset(encoder->param, "veryfast", "zerolatency");
    //开启多帧并行编码
    encoder->param->b_sliced_threads = 0;
    encoder->param->i_threads = X264_THREADS_AUTO;
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
    encoder->param->rc.i_rc_method = X264_RC_ABR;
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
}

X264Encoder::X264Encoder() {
    LOGE("X264Encoder");
    reset();
    encoder = (Encoder *) malloc(sizeof(Encoder));
    encoder->param = (x264_param_t *) malloc(sizeof(x264_param_t));
    encoder->picture = (x264_picture_t *) malloc(sizeof(x264_picture_t));
    config();
}

X264Encoder::~X264Encoder() {
    if (encoder) {
        free(encoder);
        encoder = NULL;
    }
}

bool X264Encoder::start() {
    if (INVALID != state) {
        LOGI("Start failed. Invalid state, encoder is not invalid");
        return false;
    }
    state = START;
    if ((encoder->handle = x264_encoder_open(encoder->param)) == NULL) {
        reset();
        return false;
    }
    x264_picture_alloc(encoder->picture, encoder->param->i_csp, encoder->param->i_width,
                       encoder->param->i_height);

    int y_size = encoder->param->i_width * encoder->param->i_height;
    uint8_t *buff = (uint8_t *) malloc(y_size * 3 / 2);
    encoder->picture->img.i_csp = X264_CSP_I420;
    encoder->picture->img.i_plane = 3;
    encoder->picture->img.plane[0] = buff;//Y
    encoder->picture->img.plane[1] = buff + y_size;//U
    encoder->picture->img.plane[2] = buff + y_size * 5 / 4;//V
    encoder->picture->img.i_stride[0] = encoder->param->i_width;
    encoder->picture->img.i_stride[1] = encoder->param->i_width / 2;
    encoder->picture->img.i_stride[2] = encoder->param->i_width / 2;
    return true;
}

void X264Encoder::stop() {
    if (START != state) {
        LOGI("Stop failed. Invalid state, encoder is not start");
        return;
    }
    state = STOP;
    if (encoder->picture) {
        x264_picture_clean(encoder->picture);
        free(encoder->picture);
        encoder->picture = NULL;
    }
    if (encoder->param) {
        free(encoder->param);
        encoder->param = NULL;
    }
    if (encoder->handle) {
        x264_encoder_close(encoder->handle);
    }
    LOGI("X264Encoder stop");
}

bool X264Encoder::encode(char *src, char *dest, int *s, int *type) {
    if (START != state) {
        LOGI("Start failed. Invalid state, encoder is not start");
        return 0;
    }
    s[0] = 0;
    if (!hasNalHeader) {
        hasNalHeader = true;
        return encodeHeader(dest, s, type);
    }

    encoder->picture->i_type = X264_TYPE_AUTO;
    int nNal = -1;
    x264_picture_t pic_out;
    int size = 0, i = 0;

    struct timeval start, end;
    gettimeofday(&start, NULL);
    if (!fillSrc(src)) {
        LOGE("Convert failed");
        return false;
    }
    gettimeofday(&end, NULL);
    int time = end.tv_usec - start.tv_usec;
    gettimeofday(&start, NULL);

    if (x264_encoder_encode(encoder->handle, &(encoder->nal), &nNal, encoder->picture, &pic_out) <
        0) {
        return false;
    }
//    LOGE("x264 frame size = %d", encoder->nal[0].i_payload)
//    createBuffer(env, thiz, size);
    for (i = 0; i < nNal; i++) {
        memcpy(dest, encoder->nal[i].p_payload, encoder->nal[i].i_payload);
        dest += encoder->nal[i].i_payload;
        size += encoder->nal[i].i_payload;
    }
    s[0] = size;
    type[0] = pic_out.i_type;
    gettimeofday(&end, NULL);
    LOGI("Encode type: %d, Yuv convert time: %d, Encode time: %ld", pic_out.i_type, time,
         (end.tv_usec - start.tv_usec));
    return true;
}

bool X264Encoder::flush(char *dest, int *size, int *type) {
    return false;
}

void X264Encoder::setVideoSize(int width, int height) {
    encoder->param->i_width = width; //set frame width
    encoder->param->i_height = height; //set frame height
}

void X264Encoder::setBitrate(int bitrate) {
    encoder->param->rc.i_bitrate = bitrate / 1000;
}

void X264Encoder::setFrameFormat(int format) {
    encoder->param->i_csp = format; // 设置输入的视频采样的格式
}

void X264Encoder::setFps(int fps) {
    encoder->param->i_fps_num = (uint32_t) fps;
    encoder->param->i_fps_den = 1;
}

void X264Encoder::setProfile(char *profile) {
    x264_param_apply_profile(encoder->param, profile);
}

void X264Encoder::setLevel(int level) {
    encoder->param->i_level_idc = level;// 11 12 13 20 for CIF;31 for 720P
}

bool X264Encoder::fillSrc(char *argb) {
    int width = encoder->param->i_width;
    int height = encoder->param->i_height;
    int ret = libyuv::ConvertToI420((const uint8 *) argb, width * height,
                                    encoder->picture->img.plane[0], width,
                                    encoder->picture->img.plane[1], width / 2,
                                    encoder->picture->img.plane[2], width / 2,
                                    0, 0,
                                    width, height,
                                    width, height,
                                    libyuv::kRotate0, libyuv::FOURCC_ABGR);
    return ret >= 0;
}

bool X264Encoder::encodeHeader(char *dest, int *s, int *type) {
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
    s[0] = size;
    type[0] = X264_TYPE_HEADER;
    return true;
}

void X264Encoder::reset() {
    state = INVALID;
    hasNalHeader = false;
}
