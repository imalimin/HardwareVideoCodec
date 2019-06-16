/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/Video.h"
#include "ObjectBox.h"
#include "Size.h"
#include "TimeUtils.h"
#include "../include/HwVideoFrame.h"
#include "../include/HwAudioFrame.h"

Video::Video() : HwStreamMedia() {
    name = __FUNCTION__;
    this->lock = new SimpleLock();
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Video::eventPrepare));
    registerEvent(EVENT_VIDEO_START, reinterpret_cast<EventFunc>(&Video::eventStart));
    registerEvent(EVENT_VIDEO_PAUSE, reinterpret_cast<EventFunc>(&Video::eventPause));
    registerEvent(EVENT_VIDEO_SEEK, reinterpret_cast<EventFunc>(&Video::eventSeek));
    registerEvent(EVENT_VIDEO_SET_SOURCE, reinterpret_cast<EventFunc>(&Video::eventSetSource));
    registerEvent(EVENT_VIDEO_LOOP, reinterpret_cast<EventFunc>(&Video::eventLoop));
    decoder = new AsynVideoDecoder();
}

Video::Video(HandlerThread *handlerThread) : HwStreamMedia(handlerThread) {
    name = __FUNCTION__;
    this->lock = new SimpleLock();
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Video::eventPrepare));
    registerEvent(EVENT_VIDEO_START, reinterpret_cast<EventFunc>(&Video::eventStart));
    registerEvent(EVENT_VIDEO_PAUSE, reinterpret_cast<EventFunc>(&Video::eventPause));
    registerEvent(EVENT_VIDEO_SEEK, reinterpret_cast<EventFunc>(&Video::eventSeek));
    registerEvent(EVENT_VIDEO_SET_SOURCE, reinterpret_cast<EventFunc>(&Video::eventSetSource));
    registerEvent(EVENT_VIDEO_LOOP, reinterpret_cast<EventFunc>(&Video::eventLoop));
    decoder = new AsynVideoDecoder();
}

Video::~Video() {
    LOGI("Video::~Video");
    lock->lock();
    if (audioPlayer) {
        audioPlayer->stop();
        delete audioPlayer;
        audioPlayer = nullptr;
    }
    LOGI("Video::~audioPlayer");
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (path) {
        delete[]path;
        path = nullptr;
    }
    lock->unlock();
    if (lock) {
        delete lock;
        lock = nullptr;
    }
}

bool Video::eventRelease(Message *msg) {
    LOGI("Video::eventRelease");
    post([this] {
        eventStop(nullptr);
        if (texAllocator) {
            egl->makeCurrent();
            delete texAllocator;
            texAllocator = nullptr;
        }
        if (egl) {
            delete egl;
            egl = nullptr;
        }
    });
    return true;
}

bool Video::eventPrepare(Message *msg) {
    playState = PAUSE;
    NativeWindow *nw = static_cast<NativeWindow *>(msg->tyrUnBox());
    if (decoder->prepare(path)) {
        createAudioPlayer();
    } else {
        LOGE("Video::open %s failed", path);
        return true;
    }
    post([this, nw] {
        initEGL(nw);
        wait(10000);
        lock->notify();
    });
    lock->wait();
    return true;
}

bool Video::eventStart(Message *msg) {
    LOGI("Video::eventStart");
    if (STOP != playState) {
        playState = PLAYING;
        sendLoop();
    }
    if (audioPlayer) {
        audioPlayer->flush();
    }
    if (decoder) {
        decoder->start();
    }
    return true;
}

bool Video::eventPause(Message *msg) {
    if (STOP != playState) {
        playState = PAUSE;
    }
    if (decoder) {
        decoder->pause();
    }
    return true;
}

bool Video::eventSeek(Message *msg) {
    int64_t us = msg->arg2;
    decoder->seek(us);
    return true;
}

bool Video::eventStop(Message *msg) {
    playState = STOP;
    return true;
}

bool Video::eventSetSource(Message *msg) {
    this->path = static_cast<char *>(msg->tyrUnBox());
    return true;
}

void Video::sendLoop() {
    postEvent(new Message(EVENT_VIDEO_LOOP, nullptr));
}

bool Video::eventLoop(Message *msg) {
    post([this] {
        if (PLAYING != playState) {
            return;
        }
        if (!texAllocator || !decoder) {
            eventPause(nullptr);
            return;
        }
        lock->lock();
        int ret = grab();
        lock->unlock();
        if (MEDIA_TYPE_EOF == ret) {
            eventStop(nullptr);
        }
        sendLoop();


//        if (MEDIA_TYPE_VIDEO != ret) {
//            if (MEDIA_TYPE_AUDIO == ret && audioPlayer && frame) {
//                audioPlayer->write(frame->data, frame->size);
//            }
//            return;
//        }
    });
    return true;
}

void Video::checkFilter() {
    if (!yuvFilter) {
        yuvFilter = new YUV420PFilter();
        yuvFilter->init(decoder->width(), decoder->height());
        yuv[0] = texAllocator->alloc();
        yuv[1] = texAllocator->alloc();
        yuv[2] = texAllocator->alloc();
    }
}

int Video::grab() {
    int64_t time = getCurrentTimeUS();
    HwAbsMediaFrame *frame = nullptr;
    int ret = decoder->grab(&frame);
    Logcat::i("HWVC", "Video::grab cost: %lld, ret: %d", getCurrentTimeUS() - time, ret);
    if (!frame) {
        return ret;
    }
    int64_t curPts = frame->getPts();

//    if (lastPts > 0) {
//        int64_t t = (curPts - lastPts) - (getCurrentTimeUS() - lastShowTime);
//        lock->wait(t);
//        LOGI("Video::grab %d x %d, delta time: %lld, wait time: %lld", 0, 0,
//             (getCurrentTimeUS() - lastShowTime) / 1000, t);
//    }
    lastShowTime = getCurrentTimeUS();

    lastPts = curPts;
    if (frame->isVideo()) {
//        checkFilter();
//        HwVideoFrame *videoFrame = dynamic_cast<HwVideoFrame *>(frame);
//        int size = videoFrame->getWidth() * videoFrame->getHeight();
//        egl->makeCurrent();
//        glBindTexture(GL_TEXTURE_2D, yuv[0]);
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, videoFrame->getWidth(),
//                     videoFrame->getHeight(), 0,
//                     GL_LUMINANCE,
//                     GL_UNSIGNED_BYTE,
//                     frame->getData());
//        glBindTexture(GL_TEXTURE_2D, yuv[1]);
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, videoFrame->getWidth() / 2,
//                     videoFrame->getHeight() / 2, 0,
//                     GL_LUMINANCE,
//                     GL_UNSIGNED_BYTE,
//                     frame->getData() + size);
//        glBindTexture(GL_TEXTURE_2D, yuv[2]);
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, videoFrame->getWidth() / 2,
//                     videoFrame->getHeight() / 2, 0,
//                     GL_LUMINANCE,
//                     GL_UNSIGNED_BYTE,
//                     frame->getData() + size + size / 4);
//        glBindTexture(GL_TEXTURE_2D, GL_NONE);
//
////        lock->lock();
//        glViewport(0, 0, videoFrame->getWidth(), videoFrame->getHeight());
////        lock->unlock();
//        yuvFilter->draw(yuv[0], yuv[1], yuv[2]);
//        invalidate(yuvFilter->getFrameBuffer()->getFrameTexture(), videoFrame->getWidth(),
//                   videoFrame->getHeight());
        return MEDIA_TYPE_VIDEO;
    } else if (frame->isAudio()) {
        HwAudioFrame *audioFrame = dynamic_cast<HwAudioFrame *>(frame);
        if (audioPlayer) {
            Logcat::i("HWVC", "Video::play audio: %d, %lld, %lld",
                      audioFrame->getChannels(),
                      audioFrame->getSampleCount(),
                      audioFrame->getDataSize());
            audioPlayer->write(audioFrame->getData(),
                               static_cast<size_t>(audioFrame->getDataSize()));
        }
        return MEDIA_TYPE_AUDIO;
    }
    return ret;
}

bool Video::invalidate(int tex, uint32_t width, uint32_t height) {
    Message *msg = new Message(EVENT_RENDER_FILTER, nullptr);
    msg->obj = new ObjectBox(new Size(width, height));
    msg->arg1 = yuvFilter->getFrameBuffer()->getFrameTexture();
    postEvent(msg);
    return true;
}

void Video::createAudioPlayer() {
    int format;
    switch (decoder->getSampleFormat()) {
        case AV_SAMPLE_FMT_S16:
            format = SL_PCMSAMPLEFORMAT_FIXED_16;
            break;
        case AV_SAMPLE_FMT_U8:
            format = SL_PCMSAMPLEFORMAT_FIXED_8;
            break;
        default:
            format = SL_PCMSAMPLEFORMAT_FIXED_32;
    }
    audioPlayer = new HwAudioPlayer(decoder->getChannels(),
                                    decoder->getSampleHz(),
                                    format,
                                    decoder->getSamplesPerBuffer());
    audioPlayer->start();
}

void Video::initEGL(NativeWindow *nw) {
    if (nw->egl) {
        LOGI("Video::init EGL with context");
        egl = new Egl(nw->egl, nullptr);
    } else {
        LOGI("Video::init EGL");
        egl = new Egl();
        nw->egl = egl;
    }
    egl->makeCurrent();
    if (!texAllocator) {
        texAllocator = new TextureAllocator();
    }
}