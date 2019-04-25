/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwBitmapFactory.h"
#include "../include/JpegDecoder.h"
#include "../include/PngDecoder.h"
#include "../include/Logcat.h"

HwBitmap *HwBitmapFactory::decodeFile(std::string file) {
    int ret = 0;
    int width = 0, height = 0;
    uint8_t *rgba = nullptr;
    PngDecoder *pDecoder = new PngDecoder();
    ret = pDecoder->decodeFile(file, &rgba, &width, &height);//先尝试以png进行解码
    delete pDecoder;
    if (ret <= 0) {//解码失败则使用jpeg解码
        JpegDecoder *jDecoder = new JpegDecoder();
        ret = jDecoder->decodeFile(file, &rgba, &width, &height);
        delete jDecoder;
    }
    if (!ret || 0 == width || 0 == height) {
        Logcat::i("HWVC", "HwBitmapFactory decodeFile %s failed", file.c_str());
        return nullptr;
    }
    HwBitmap *bitmap = HwBitmap::create(width, height, ImageFormat::RGBA);
    memcpy(bitmap->getPixels(), rgba, static_cast<size_t>(bitmap->getByteSize()));
    delete[]rgba;//这里重复申请了一次内存，待优化
    return bitmap;
}

HwBitmap *HwBitmapFactory::decodeFile(std::string file, HwBitmap *recycleBitmap) {
    return nullptr;
}