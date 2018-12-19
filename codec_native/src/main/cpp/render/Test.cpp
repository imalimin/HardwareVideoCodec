//
// Created by lmy on 2018/12/19.
//

#include <jni.h>
#include <log.h>
#include "Render.h"

Render *render = nullptr;
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_lmy_samplenative_MainActivity_addMessage
        (JNIEnv *env, jobject thiz) {
    if (nullptr == render) {
        render = new Render();
    }
    render->post();
}

#ifdef __cplusplus
}
#endif
