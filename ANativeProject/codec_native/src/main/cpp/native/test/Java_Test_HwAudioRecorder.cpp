//
// Created by limin on 2019/6/7.
//


#include <jni.h>
#include <log.h>
#include "../include/HwAudioRecorder.h"

#ifdef __cplusplus
extern "C" {
#endif

static HwAudioRecorder *recorder = nullptr;
static EventPipeline *pipeline = nullptr;
FILE *pcmFile = nullptr;
SimpleLock recorderLock;

static void loop() {
    recorderLock.lock();
    if (pipeline && recorder) {
        pipeline->queueEvent([] {
            HwBuffer *buffer = recorder->read(8192);
            if (pcmFile) {
                Logcat::i("HWVC", "TestHwAudioRecorder write %d", buffer->size());
                fwrite(buffer->getData(), 1, buffer->size(), pcmFile);
            }
            loop();
        });
    }
    recorderLock.unlock();
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_TestHwAudioRecorder_start
        (JNIEnv *env, jobject thiz) {
    if (!pipeline) {
        pipeline = new EventPipeline("AudioRecorderTest");
    }
    if (recorder) {
        recorder->stop();
        recorder = nullptr;
    }
    if (pcmFile) {
        fclose(pcmFile);
        pcmFile = nullptr;
    }
    pcmFile = fopen("/sdcard/pcm_sl_read.pcm", "wb");
    recorder = new HwAudioRecorder(2, 48000, SL_PCMSAMPLEFORMAT_FIXED_32, 1024);
    recorder->start();
    loop();
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_TestHwAudioRecorder_play
        (JNIEnv *env, jobject thiz) {
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

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_TestHwAudioRecorder_stop
        (JNIEnv *env, jobject thiz) {
}

#ifdef __cplusplus
}
#endif