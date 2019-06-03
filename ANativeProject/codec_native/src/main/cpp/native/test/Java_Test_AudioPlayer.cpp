/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/


#include <jni.h>
#include <log.h>
#include "../include/AudioPlayer.h"
#include "../include/EventPipeline.h"
#include <mutex>

#ifdef __cplusplus
extern "C" {
#endif

static AudioPlayer *player = nullptr;
static FILE *file = nullptr;
static EventPipeline *pipeline = nullptr;
static int index = 0;
SimpleLock simpleLock;
SimpleLock waitLock;

static void loopTest() {
    simpleLock.lock();
    if (!file || !pipeline) {
        simpleLock.unlock();
        return;
    }
    pipeline->queueEvent([] {
        if (!player) {
            return;
        }
        uint8_t data[8192];
        int ret = fread(data, 1, 8192, file);
        if (ret > 0) {
            ++index;
            player->write(data, 8192);
            loopTest();
        }
    });
    simpleLock.unlock();
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_media_AudioPlayerTest_start
        (JNIEnv *env, jobject thiz) {
    pipeline = new EventPipeline("AudioPlayerTest");
    file = fopen("/sdcard/2.pcm", "rb");
    if (file) {
        pipeline->queueEvent([] {
            Logcat::e("HWVC", "AudioPlayerTest_start");
            player = new AudioPlayer(2, 48000, SL_PCMSAMPLEFORMAT_FIXED_32, 1024);
            player->start();
            loopTest();
        });
    }
}
JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_media_AudioPlayerTest_stop
        (JNIEnv *env, jobject thiz) {
    pipeline->queueEvent([] {
        if (player) {
            player->stop();
            delete player;
            player = nullptr;
        }
        if (file) {
            fclose(file);
            file = nullptr;
        }
    });
    simpleLock.lock();
    delete pipeline;
    pipeline = nullptr;
    simpleLock.unlock();
}

#ifdef __cplusplus
}
#endif