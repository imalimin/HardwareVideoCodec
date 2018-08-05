/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <string.h>

#ifndef HARDWAREVIDEOCODEC_EVENT_H
#define HARDWAREVIDEOCODEC_EVENT_H

class Message {
public:
    int what;
    int arg1;
    long arg2;
    void *obj;

    void (*handle)(Message *);

    Message(void (handle)(Message *));

    ~Message();

    /**
     * 如果void *指向一个class类，那么系统由于认为void *指向一个普通的内存空间，
     * 所以释放指针时系统class的析构函数不会调用。
     * 这里定义一个内敛函数用于释放obj
     * @tparam T
     * @param obj
     */
    template<typename T>
    inline void releaseObject() {
        if (NULL != obj) {
            delete (T *) obj;
            obj = NULL;
        }
    }
};

Message *obtainMessage(int what, void *obj, void (handle)(Message *));

Message *obtainMessage(int what, int arg1, long arg2, void *obj, void (handle)(Message *));

#endif //HARDWAREVIDEOCODEC_EVENT_H
