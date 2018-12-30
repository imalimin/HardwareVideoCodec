/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_FILTERENTITY_H
#define HARDWAREVIDEOCODEC_FILTERENTITY_H

#include "Object.h"
#include <map>
#include <string>

using namespace std;

class FilterEntity : public Object {
public:
    int version = 1;
    string name;
    string vertex;
    string fragment;
    map<string, float> params;
    map<string, string> samplers;

    FilterEntity();

    ~FilterEntity();
};


#endif //HARDWAREVIDEOCODEC_FILTERENTITY_H
