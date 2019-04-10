/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_VIDEOPROCESSOR_H
#define HARDWAREVIDEOCODEC_VIDEOPROCESSOR_H

#include "Object.h"
#include "UnitPipeline.h"
#include "Screen.h"
#include "Filter.h"
#include "HwWindow.h"

class VideoProcessor : public Object {
public:
    VideoProcessor();

    virtual ~VideoProcessor();

    void setSource(char *path);

    void prepare(HwWindow *win, int width, int height);

    void start();

    void pause();

    void seek(int64_t us);

    void setFilter(Filter *filter);

private:
    UnitPipeline *pipeline = nullptr;
    HandlerThread *unitHandler = nullptr;
    HandlerThread *screenHandler = nullptr;
};


#endif //HARDWAREVIDEOCODEC_VIDEOPROCESSOR_H
