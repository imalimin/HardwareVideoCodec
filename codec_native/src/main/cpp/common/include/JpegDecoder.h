/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_JPEGDECODER_H
#define HARDWAREVIDEOCODEC_JPEGDECODER_H

#include <string>
#include <fstream>
#include "Object.h"
#include "libjpeg-turbo/jpeglib.h"
#include "libjpeg-turbo/turbojpeg.h"

using namespace std;

class JpegDecoder : public Object {
public:
    JpegDecoder();

    virtual ~JpegDecoder();

    bool decodeFile(string file, uint8_t **rgb, int *width, int *height);

private:
    tjhandle handle;

    unsigned long readFile(string file, uint8_t **buffer);
};


#endif //HARDWAREVIDEOCODEC_JPEGDECODER_H
