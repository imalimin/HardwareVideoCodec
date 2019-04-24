/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWFRAMEALLOCATOR_H
#define HARDWAREVIDEOCODEC_HWFRAMEALLOCATOR_H

#include "Object.h"
#include "HwAbsFrame.h"
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

    HwAbsFrame *ref(AVFrame *avFrame);

    HwAbsFrame *ref(HwAbsFrame *src);

    void unRef(HwAbsFrame **frame);

    void printInfo() {
        Logcat::i("HWVC", "HwFrameAllocator::info: %d, %d", refQueue.size(), unRefQueue.size());
    }

private:
    list<HwAbsFrame *> refQueue;
    list<HwAbsFrame *> unRefQueue;

    HwAbsFrame *refVideo(AVFrame *avFrame);

    HwAbsFrame *refAudio(AVFrame *avFrame);

    void copyInfo(HwAbsFrame *dest, AVFrame *src);
};


#endif //HARDWAREVIDEOCODEC_HWFRAMEALLOCATOR_H
