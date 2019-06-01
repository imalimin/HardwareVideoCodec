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

#define MemFIFOTest_FRAME_SIZE 8
HwFIFOBuffer *fifo = nullptr;
uint8_t *data = nullptr;
int index = 'a';
int pushCount = 0;
int takeCount = 0;

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
    ++pushCount;
    Logcat::i("HWVC", "MemFIFOTest_push: %c,%c,%c,%c,%c,%c,%c,%c, %d",
              (char) data[0], (char) data[1],
              (char) data[2], (char) data[3],
              (char) data[4], (char) data[5],
              (char) data[6], (char) data[7],
              pushCount);
    fifo->push(data, MemFIFOTest_FRAME_SIZE);
    ++index;
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_test_MemFIFOTest_take
        (JNIEnv *env, jobject thiz) {
    if (fifo) {
        HwBuffer *buf = fifo->take(MemFIFOTest_FRAME_SIZE);
        if (buf) {
            ++takeCount;
            Logcat::i("HWVC", "MemFIFOTest_take: %c,%c,%c,%c,%c,%c,%c,%c, %lld, %d",
                      (char) buf->getData()[0], (char) buf->getData()[1],
                      (char) buf->getData()[2], (char) buf->getData()[3],
                      (char) buf->getData()[4], (char) buf->getData()[5],
                      (char) buf->getData()[6], (char) buf->getData()[7],
                      buf->size(), takeCount);
        }
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