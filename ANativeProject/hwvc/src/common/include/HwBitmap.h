/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWBITMAP_H
#define HARDWAREVIDEOCODEC_HWBITMAP_H

#include "Object.h"
#include "ImageFormat.h"
#include "HwResult.h"

class HwBitmap : public Object {
public:
    static HwBitmap *create(int width, int height, ImageFormat format);

    static float getImageFormatBytes(ImageFormat format);

    HwBitmap(int width, int height, ImageFormat format);

    virtual ~HwBitmap();

    int getWidth();

    int getHeight();

    /**
     * 改变HwBitmap大小和格式，用于内存复用
     */
    HwResult resize(int width, int height, ImageFormat format);

    /**
     * 获取指向图片像素内存的指针
     */
    uint8_t *getPixels();

    /**
     * 获取图片占用的内存大小
     */
    uint64_t getByteSize();

private:

    void config();

private:
    uint8_t *pixels = nullptr;//用于存储图片像素的buffer
    uint64_t byteSize = 0;
    int width = 0;
    int height = 0;
    ImageFormat format = ImageFormat::NONE;
};


#endif //HARDWAREVIDEOCODEC_HWBITMAP_H
