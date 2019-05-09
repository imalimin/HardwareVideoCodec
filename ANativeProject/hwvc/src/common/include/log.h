/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "Logcat.h"

#define LOGE(format, ...)  Logcat::e("HWVC", format, ##__VA_ARGS__)
#define LOGI(format, ...)  Logcat::i( "HWVC", format, ##__VA_ARGS__)