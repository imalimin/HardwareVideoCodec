//
// Created by limin on 2019/6/7.
//


#include <jni.h>
#include <log.h>
#include "../include/HwAudioRecorder.h"
#include "../include/HwAudioPlayer.h"

#ifdef __cplusplus
extern "C" {
#endif

static HwAudioRecorder *recorder = nullptr;
static HwAudioPlayer *player = nullptr;
static EventPipeline *pipeline = nullptr;
FILE *pcmFile = nullptr;
SimpleLock recorderLock;

static void stop() {
    if (recorder) {
        recorderLock.lock();
        recorder->stop();
        delete recorder;
        recorder = nullptr;
        if (pcmFile) {
            fclose(pcmFile);
            pcmFile = nullptr;
        }
        recorderLock.unlock();
    }
    if (player) {
        recorderLock.lock();
        player->stop();
        delete player;
        player = nullptr;
        if (pcmFile) {
            fclose(pcmFile);
            pcmFile = nullptr;
        }
        recorderLock.unlock();
    }
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
}

static void loop() {
    if (pipeline && recorder) {
        pipeline->queueEvent([] {
            recorderLock.lock();
            if (recorder) {
                HwBuffer *buffer = recorder->read(8192);
                if (pcmFile) {
                    Logcat::i("HWVC", "TestHwAudioRecorder write %d", buffer->size());
                    fwrite(buffer->getData(), 1, buffer->size(), pcmFile);
                }
            }
            recorderLock.unlock();
            loop();
        });
    } else if (pipeline && player) {
        pipeline->queueEvent([] {
            recorderLock.lock();
            if (player) {
                uint8_t *buf = new uint8_t[8192];
                size_t size = fread(buf, 1, 8192, pcmFile);
                if (size <= 0) {
                    recorderLock.unlock();
                    stop();
                    return;
                }
                player->write(buf, size);
                delete[] buf;
            }
            recorderLock.unlock();
            loop();
        });
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_TestHwAudioRecorder_start
        (JNIEnv *env, jobject thiz) {
    stop();
    if (!pipeline) {
        pipeline = new EventPipeline("AudioRecorderTest");
    }
    pcmFile = fopen("/sdcard/pcm_sl_read.pcm", "wb");
    recorder = new HwAudioRecorder(2, 48000, SL_PCMSAMPLEFORMAT_FIXED_32, 1024);
    recorder->start();
    loop();
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_TestHwAudioRecorder_play
        (JNIEnv *env, jobject thiz) {
    stop();
    if (!pipeline) {
        pipeline = new EventPipeline("AudioRecorderTest");
    }
    pcmFile = fopen("/sdcard/pcm_sl_read.pcm", "rb");
    player = new HwAudioPlayer(2, 48000, SL_PCMSAMPLEFORMAT_FIXED_32, 1024);
    player->start();
    loop();
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_TestHwAudioRecorder_stop
        (JNIEnv *env, jobject thiz) {
    stop();
}

#ifdef __cplusplus
}
#endif