/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/StringUtils.h"
#include "log.h"


namespace StringUtils {
    int jStringArray2StringArray(JNIEnv *env, jobjectArray jStringArray, string **array) {
        int len = env->GetArrayLength(jStringArray);
//        *array = new string[len];
        for (int i = 0; i < len; i++) {
            jstring obj = (jstring) env->GetObjectArrayElement(jStringArray, i);
            const char *str = env->GetStringUTFChars(obj, NULL);
//            *array[i] = str;
            env->ReleaseStringUTFChars(obj, str);
            LOGI("jStringArray2StringArray: %s", str);
        }
        return len;
    }
}