/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/NormalDrawer.h"

static const string VERTEX = SHADER(

);
static const string FRAGMENT = SHADER(

);

NormalDrawer::NormalDrawer() {
    createProgram(VERTEX, FRAGMENT);
}

NormalDrawer::~NormalDrawer() {

}
