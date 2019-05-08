/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWFRAMEALLOCATOR_H
#define HARDWAREVIDEOCODEC_HWFRAMEALLOCATOR_H

#include "Object.h"
#include "HwAbsMediaFrame.h"
#include <list>
#include "Logcat.h"

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif
#include "ff/libavformat/avformat.h"
#ifdef __cplusplus
}
#endif

class HwFrameAllocator : public Object {
public:
    HwFrameAllocator();

    ~HwFrameAllocator();

    HwAbsMediaFrame *ref(AVFrame *avFrame);

    HwAbsMediaFrame *ref(HwAbsMediaFrame *src);

    void unRef(HwAbsMediaFrame **frame);

    void printInfo() {
        Logcat::i("HWVC", "HwFrameAllocator::info: %d, %d", refQueue.size(), unRefQueue.size());
    }

private:
    list<HwAbsMediaFrame *> refQueue;
    list<HwAbsMediaFrame *> unRefQueue;

    HwAbsMediaFrame *refVideo(AVFrame *avFrame);

    HwAbsMediaFrame *refAudio(AVFrame *avFrame);

    void copyInfo(HwAbsMediaFrame *dest, AVFrame *src);
};


#endif //HARDWAREVIDEOCODEC_HWFRAMEALLOCATOR_H
