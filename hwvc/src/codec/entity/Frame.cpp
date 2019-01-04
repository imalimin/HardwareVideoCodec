/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <malloc.h>
#include "../include/Frame.h"

#ifdef __cplusplus
extern "C" {
#endif
Frame::Frame(int w, int h) {
    this->width = w;
    this->height = h;
    data = (uint8_t *) malloc(w * h * 3 / 2);
}

Frame::~Frame() {
    free(data);
    this->width = 0;
    this->height = 0;
}
#ifdef __cplusplus
}
#endif