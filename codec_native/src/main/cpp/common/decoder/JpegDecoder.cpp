/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/JpegDecoder.h"

JpegDecoder::JpegDecoder() {
    handle = tjInitDecompress();
}

JpegDecoder::~JpegDecoder() {
    tjDestroy(handle);
}

int JpegDecoder::decodeFile(string file, uint8_t **rgb, int *width, int *height) {
    uint8_t *buffer;
    unsigned long length = readFile(file, &buffer);
    if (0 == length) {
        return 0;
    }

    int subsample, colorspace;
    int flags = 0;
    int fmt = TJPF_RGBA;
    int channels = 4;
    tjDecompressHeader3(handle, buffer, length, width, height, &subsample, &colorspace);

    flags |= 0;
    *rgb = new uint8_t[(*width) * (*height) * channels];
    tjDecompress2(handle, buffer, length, *rgb, *width, 0, *height, fmt, flags);
    delete[]buffer;
    return 1;
}