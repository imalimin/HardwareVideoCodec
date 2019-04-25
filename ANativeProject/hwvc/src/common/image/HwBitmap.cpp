/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwBitmap.h"
#include "../include/Logcat.h"

HwBitmap *HwBitmap::create(int width, int height, ImageFormat format) {
    if (ImageFormat::NONE == format) {
        Logcat::e("HWVC", "Invalid image format!");
        return nullptr;
    }
    return new HwBitmap(width, height, format);
}

float HwBitmap::getImageFormatBytes(ImageFormat format) {
    switch (format) {
        case ImageFormat::RGB:
            return 3;
        case ImageFormat::RGBA:
            return 4;
        case ImageFormat::NV12:
        case ImageFormat::NV21:
            return 1.5;
        default:
            return 0;
    }
}

HwBitmap::HwBitmap(int width, int height, ImageFormat format) {
    this->width = width;
    this->height = height;
    this->format = format;
    config();
}

void HwBitmap::config() {
    if (ImageFormat::NONE == format) {
        Logcat::e("HWVC", "Invalid image format!");
        return;
    }
    byteSize = static_cast<uint64_t>(getByteSize());
    pixels = new uint8_t[byteSize];
}

HwBitmap::~HwBitmap() {
    if (pixels) {
        delete[]pixels;
        pixels = nullptr;
    }
    byteSize = 0;
    width = 0;
    height = 0;
    format = ImageFormat::NONE;
}

int HwBitmap::getWidth() { return width; }

int HwBitmap::getHeight() { return height; }

HwResult HwBitmap::resize(int width, int height, ImageFormat format) {
    if (ImageFormat::NONE == format) {
        Logcat::e("HWVC", "Invalid image format!");
        return Hw::FAILED;
    }
    int64_t byteSize = static_cast<uint64_t>(width * height * getImageFormatBytes(format));
    if (byteSize > this->byteSize) {
        return Hw::FAILED;
    }
    this->width = width;
    this->height = height;
    this->format = format;
    if (!this->pixels) {
        this->byteSize = getByteSize();
        this->pixels = new uint8_t[this->byteSize];
    }
    return Hw::SUCCESS;
}

uint8_t *HwBitmap::getPixels() { return pixels; }

uint64_t HwBitmap::getByteSize() {
    return static_cast<uint64_t>(width * height * getImageFormatBytes(format));
}