/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <zconf.h>
#include "../include/PngDecoder.h"
#include "../include/log.h"

#define PNG_CHECK_BYTES 4

PngDecoder::PngDecoder() {
    handler = png_create_read_struct(PNG_LIBPNG_VER_STRING, (png_voidp) NULL, NULL, NULL);
    if (!handler) {
        release();
        LOGE("PngDecoder init failed");
    }
    infoHandler = png_create_info_struct(handler);
    if (!infoHandler) {
        release();
        LOGE("PngDecoder init failed");
    }
}

PngDecoder::~PngDecoder() {
    release();
}

static void readCallback(png_structp handler, png_bytep data, png_size_t length) {
    ImageSource *src = (ImageSource *) png_get_io_ptr(handler);

    if (src->offset + length <= src->size) {
        memcpy(data, src->data + src->offset, length);
        src->offset += length;
    } else {
        LOGE("PNG read buf failed");
        png_error(handler, "pngReaderCallback failed");
    }
}

static void fillBuffer(uint8_t **rgba, int w, int h, png_bytep *row, int channels, int color_type) {
    *rgba = new uint8_t[w * h * 4];
    if (channels == 4 || color_type == PNG_COLOR_TYPE_RGB_ALPHA) {
        for (int i = 0; i < h; ++i) {
            memcpy(*rgba + i * w * 4, row[i], w * 4);
        }
    } else if (channels == 3 || color_type == PNG_COLOR_TYPE_RGB) {
        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w; ++j) {
                (*rgba)[i * w * 4 + j * 4] = row[i][j * 3];
                (*rgba)[i * w * 4 + j * 4 + 1] = row[i][j * 3 + 1];
                (*rgba)[i * w * 4 + j * 4 + 2] = row[i][j * 3 + 2];
                (*rgba)[i * w * 4 + j * 4 + 3] = 255;
            }
        }
    }
}

int PngDecoder::decodeFile(string path, uint8_t **rgba, int *width, int *height) {
    FILE *pic_fp;
    pic_fp = fopen(path.c_str(), "rb");
    if (NULL == pic_fp)
        return 0;
    setjmp(png_jmpbuf(handler));
    uint8_t *buf = new uint8_t[PNG_CHECK_BYTES];
    fread(buf, 1, PNG_CHECK_BYTES, pic_fp);
    int ret = png_sig_cmp(buf, (png_size_t) 0, PNG_CHECK_BYTES);
    if (0 != ret) {
        fclose(pic_fp);
        LOGE("%s not a png(%d)", path.c_str(), ret);
        return -1;//不是png文件
    }
    rewind(pic_fp);
    png_init_io(handler, pic_fp);
    png_read_png(handler, infoHandler, PNG_TRANSFORM_EXPAND, 0);
    png_bytep *row = png_get_rows(handler, infoHandler);
    int channels = png_get_channels(handler, infoHandler); // 获取通道数
    int bit_depth = png_get_bit_depth(handler, infoHandler); // 获取位深
    int color_type = png_get_color_type(handler, infoHandler); // 颜色类型
    *width = png_get_image_width(handler, infoHandler);
    *height = png_get_image_height(handler, infoHandler);
    int w = *width, h = *height;
    LOGI("PNG channels=%d, depth=%d, type=%d, %d x %d", channels, bit_depth, color_type, w, h);

    fillBuffer(rgba, w, h, row, channels, color_type);

    fclose(pic_fp);
    return 1;
}

int PngDecoder::decodeBuf(uint8_t *pngBuf, int bufSize, uint8_t **rgba, int *width, int *height) {
    if (setjmp(png_jmpbuf(handler))) {
        release();
        LOGE("PNG setjmp failed");
    }
    ImageSource src;
    src.data = pngBuf;
    src.size = bufSize;
    src.offset = 0;
    png_set_read_fn(handler, &src, readCallback);
    png_read_png(handler, infoHandler, PNG_TRANSFORM_EXPAND, 0);
    png_bytep *row = png_get_rows(handler, infoHandler);
    int channels = png_get_channels(handler, infoHandler); // 获取通道数
    int bit_depth = png_get_bit_depth(handler, infoHandler); // 获取位深
    int color_type = png_get_color_type(handler, infoHandler); // 颜色类型
    *width = png_get_image_width(handler, infoHandler);
    *height = png_get_image_height(handler, infoHandler);
    int w = *width, h = *height;
    LOGI("PNG channels=%d, depth=%d, type=%d, %d x %d", channels, bit_depth, color_type, w, h);

    fillBuffer(rgba, w, h, row, channels, color_type);
    return 1;
}

void PngDecoder::release() {
    if (infoHandler) {
        png_destroy_info_struct(handler, &infoHandler);
    }
    if (handler) {
        png_destroy_read_struct(&handler, (png_infopp) NULL, (png_infopp) NULL);
    }
}