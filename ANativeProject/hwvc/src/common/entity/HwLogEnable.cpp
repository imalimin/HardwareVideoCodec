/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/HwLogEnable.h"

HwLogEnable::HwLogEnable() : Object() {
    logEnable = false;
}

HwLogEnable::~HwLogEnable() {
    logEnable = false;
}

void HwLogEnable::setLogEnable(bool enable) {
    this->logEnable = enable;
}

bool HwLogEnable::isLogEnable() {
    return logEnable;
}