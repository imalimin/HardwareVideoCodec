/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../../../../../codec/src/main/jni/codec/log.h"
#include <string.h>

template<class T>
class BlockQueue {
public:
    void offer(T entity) {

    }

    T take() {
        return NULL;
    }

    int size() {
        return 0;
    }

    bool isEmpty() {
        return 0 == size();
    }

private:
//    std::list<T> queue;
//    std::mutex mutex;
};
