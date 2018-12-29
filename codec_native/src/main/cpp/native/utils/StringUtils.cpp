/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/StringUtils.h"
#include "log.h"


namespace StringUtils {
    char **jStringArray2StringArray(JNIEnv *env, jobjectArray jStringArray, int &size) {
        int len = env->GetArrayLength(jStringArray);
        char **array = static_cast<char **>(malloc(sizeof(char **)));
        *array = static_cast<char *>(malloc(sizeof(char *) * len));
        for (int i = 0; i < len; i++) {
            jstring obj = (jstring) env->GetObjectArrayElement(jStringArray, i);
            int l = env->GetStringUTFLength(obj) + 1;
            const char *str = env->GetStringUTFChars(obj, NULL);
            char *tmp = static_cast<char *>(malloc(sizeof(char) * l));
            memcpy(tmp, str, l);
            array[i] = tmp;
            env->ReleaseStringUTFChars(obj, str);
//            LOGI("jStringArray2StringArray: %s", array[i]);
        }
        size = len;
        return array;
    }

    void releaseStringArray(char **array, int size) {
        for (int i = 0; i < size; ++i) {
            free(array[i]);
            free(*array);
            free(array);
        }
    }
}