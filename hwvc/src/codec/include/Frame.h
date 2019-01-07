/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_FRAME_H
#define HARDWAREVIDEOCODEC_FRAME_H

#include "Object.h"
#include <cstdint>

#ifdef __cplusplus
extern "C" {
#endif
class Frame : public Object {
public:
    Frame(int w, int h);

    ~Frame();

    uint8_t *data = nullptr;
    int width = 0;
    int height = 0;
    int channels = 0;
    int offset = 0;
    int size = 0;
};
#ifdef __cplusplus
}
#endif


#endif //HARDWAREVIDEOCODEC_FRAME_H
