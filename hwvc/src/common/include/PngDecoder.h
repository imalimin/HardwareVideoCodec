/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_PNGDECODER_H
#define HARDWAREVIDEOCODEC_PNGDECODER_H


#include <string>
#include "Object.h"
#include "FileUtils.h"
#include "libpng/png.h"

class PngDecoder : public Object {
public:
    PngDecoder();

    virtual ~PngDecoder();


    /**
     *
     * @return 0:failed, 1:success, -1:not png file
     */
    int decodeFile(string path, uint8_t **rgba, int *width, int *height);

private:
    png_structp handler;
    png_infop infoHandler;

    void release();
};


#endif //HARDWAREVIDEOCODEC_PNGDECODER_H
