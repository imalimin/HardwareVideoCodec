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

bool JpegDecoder::decodeFile(string file, uint8_t **rgb, int *width, int *height) {
    uint8_t *buffer;
    unsigned long length = readFile(file, &buffer);
    if (0 == length) {
        return false;
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
    return true;
}

unsigned long JpegDecoder::readFile(string file, uint8_t **buffer) {
    ifstream infile;
    infile.open(file.data());
    if (!infile.is_open()) return 0;

    infile.seekg(0, ios::end);
    unsigned long length = static_cast<unsigned long>(infile.tellg());
    infile.seekg(0, ios::beg);
    *buffer = new uint8_t[length];
    infile.read(reinterpret_cast<char *>(*buffer), length);
    return length;
}