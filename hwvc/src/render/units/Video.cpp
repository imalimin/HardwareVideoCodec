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

Video::Video() {
    name = "Video";
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Video::eventPrepare));
    registerEvent(EVENT_VIDEO_START, reinterpret_cast<EventFunc>(&Video::eventStart));
    registerEvent(EVENT_VIDEO_PAUSE, reinterpret_cast<EventFunc>(&Video::eventPause));
    registerEvent(EVENT_VIDEO_SEEK, reinterpret_cast<EventFunc>(&Video::eventSeek));
    registerEvent(EVENT_VIDEO_SET_SOURCE, reinterpret_cast<EventFunc>(&Video::eventSetSource));
    decoder = new AsynVideoDecoder();
}

Video::~Video() {
    eventStop(nullptr);
    release();
    LOGI("Video::~Image");
}

void Video::release() {
    Unit::release();
    eventStop(nullptr);
    if (audioPlayer) {
        audioPlayer->stop();
        delete audioPlayer;
        audioPlayer = nullptr;
    }
    pipeline->queueEvent([=] {
        if (texAllocator) {
            delete texAllocator;
            texAllocator = nullptr;
        }
        if (egl) {
            delete egl;
            egl = nullptr;
        }
    });
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
    if (frame) {
        delete frame;
        frame = nullptr;
    }
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (path) {
        delete[]path;
        path = nullptr;
    }
}

bool Video::eventPrepare(Message *msg) {
    playState = PAUSE;
    if (!pipeline) {
        pipeline = new EventPipeline(name);
    }
    if (decoder->prepare(path)) {
        createAudioPlayer();
        NativeWindow *nw = static_cast<NativeWindow *>(msg->tyrUnBox());
        initEGL(nw);
    } else {
        LOGE("Video::open %s failed", path);
    }
    return true;
}

bool Video::eventStart(Message *msg) {
    LOGI("Video::eventStart");
    if (STOP != playState) {
        playState = PLAYING;
        loop();
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
    pipeline->queueEvent([this, us] {
        decoder->seek(us);
    });
    return true;
}

bool Video::eventStop(Message *msg) {
    playState = STOP;
    return true;
}

bool Video::eventInvalidate(Message *m) {
    Message *msg = new Message(EVENT_RENDER_FILTER, nullptr);
    msg->obj = new ObjectBox(new Size(frame->width, frame->height));
    msg->arg1 = yuvFilter->getFrameBuffer()->getFrameTexture();
    postEvent(msg);
    return true;
}

bool Video::eventSetSource(Message *msg) {
    this->path = static_cast<char *>(msg->tyrUnBox());
    return true;
}

void Video::loop() {
    pipeline->queueEvent([this] {
        if (PLAYING != playState)
            return;
        if (!texAllocator || !decoder) {
            eventPause(nullptr);
            return;
        }
        loop();
        egl->makeCurrent();
        checkFilter();
        int ret = grab();
        if (MEDIA_TYPE_VIDEO != ret) {
            if (MEDIA_TYPE_AUDIO == ret) {
                audioPlayer->write(frame->data, frame->size);
            }
            return;
        }
        glViewport(0, 0, frame->width, frame->height);
        yuvFilter->draw(yuv[0], yuv[1], yuv[2]);
        eventInvalidate(nullptr);
    });
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
    if (!frame) {
        frame = new Frame(decoder->width(), decoder->height());
    }
    int ret = decoder->grab(frame);
    if (MEDIA_TYPE_VIDEO != ret) {
        LOGI("grab ret=%d", ret);
        return ret;
    }
    LOGI("Video::grab %d x %d, delta time: %lld", frame->width, frame->height,
         (getCurrentTimeUS() - lastShowTime) / 1000);
    if (lastPts > 0) {
        int64_t t = (frame->pts - lastPts) - (getCurrentTimeUS() - lastShowTime);
        lock.wait(t);
    }
    lastShowTime = getCurrentTimeUS();
    lastPts = frame->pts;
    int size = frame->width * frame->height;
    glBindTexture(GL_TEXTURE_2D, yuv[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, frame->width, frame->height, 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 frame->data);
    glBindTexture(GL_TEXTURE_2D, yuv[1]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, frame->width / 2, frame->height / 2, 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 frame->data + size);
    glBindTexture(GL_TEXTURE_2D, yuv[2]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, frame->width / 2, frame->height / 2, 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 frame->data + size + size / 4);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
    return MEDIA_TYPE_VIDEO;
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
    audioPlayer = new AudioPlayer(decoder->getChannels(),
                                  decoder->getSampleHz(),
                                  format,
                                  decoder->getPerSampleSize());
    audioPlayer->start();
}

void Video::initEGL(NativeWindow *nw) {
    pipeline->queueEvent([=] {
        if (nw->egl) {
            egl = new Egl(nw->egl, nullptr);
        } else {
            egl = new Egl();
            nw->egl = egl;
        }
        egl->makeCurrent();
        if (!texAllocator) {
            texAllocator = new TextureAllocator();
        }
        pipeline->wait(10000);
        lock.notify();
    });
    lock.wait();
}