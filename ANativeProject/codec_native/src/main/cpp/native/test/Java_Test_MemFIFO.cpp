//
// Created by limin on 2019/5/7.
//

#include <jni.h>
#include <log.h>
#include "HwFIFOBuffer.h"
#include "Logcat.h"

#ifdef __cplusplus
extern "C" {
#endif

#define MemFIFOTest_FRAME_SIZE 8192
HwFIFOBuffer *fifo = nullptr;
uint8_t *data = nullptr;
int index = 'a';

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_MemFIFOTest_push
        (JNIEnv *env, jobject thiz) {
    if (!fifo) {
        fifo = new HwFIFOBuffer(8 * MemFIFOTest_FRAME_SIZE);
        data = new uint8_t[MemFIFOTest_FRAME_SIZE];
    }
    if (index > 'z') {
        index = 'a';
    }
    memset(data, index, MemFIFOTest_FRAME_SIZE);
    Logcat::i("HWVC", "MemFIFOTest_push: %c", (char) data[0]);
    fifo->push(data, MemFIFOTest_FRAME_SIZE);
    ++index;
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_MemFIFOTest_take
        (JNIEnv *env, jobject thiz) {
    if (fifo) {
        HwAbsFrame *frame = fifo->take(MemFIFOTest_FRAME_SIZE);
        Logcat::i("HWVC", "MemFIFOTest_take: %c, %lld",
                  (char) frame->getData()[0],
                  frame->getDataSize());
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_MemFIFOTest_release
        (JNIEnv *env, jobject thiz) {
    if (fifo) {
        delete fifo;
        fifo = nullptr;
    }
    if (data) {
        delete[] data;
        data = nullptr;
    }
}

#ifdef __cplusplus
}
#endif