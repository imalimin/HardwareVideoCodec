/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_VIDEO_H
#define HARDWAREVIDEOCODEC_VIDEO_H

#include "HwStreamMedia.h"
#include "TextureAllocator.h"
#include "AsynVideoDecoder.h"
#include "YUV420PFilter.h"
#include "EventPipeline.h"
#include "Egl.h"
#include "HwAbsMediaFrame.h"
#include "AudioPlayer.h"
#include "SimpleLock.h"
#include "../entity/NativeWindow.h"

class Video : public HwStreamMedia {
public:
    Video();

    Video(HandlerThread *handlerThread);

    virtual ~Video();

    bool eventRelease(Message *msg) override;

    bool eventPrepare(Message *msg);

    bool eventStart(Message *msg);

    bool eventPause(Message *msg);

    bool eventSeek(Message *msg);

    bool eventStop(Message *msg);

    bool eventSetSource(Message *msg);

    bool eventLoop(Message *msg);

    bool invalidate(int tex, uint32_t width, uint32_t height);

private:
    Egl *egl = nullptr;
    TextureAllocator *texAllocator = nullptr;
    AsynVideoDecoder *decoder = nullptr;
    YUV420PFilter *yuvFilter = nullptr;
    GLuint yuv[3];
    PlayState playState = STOP;
    SimpleLock *lock;
    char *path;
    AudioPlayer *audioPlayer = nullptr;
    int64_t lastPts = 0;
    int64_t lastShowTime = 0;

    void sendLoop();

    void checkFilter();

    int grab();

    void createAudioPlayer();

    void initEGL(NativeWindow *nw);
};


#endif //HARDWAREVIDEOCODEC_VIDEO_H
