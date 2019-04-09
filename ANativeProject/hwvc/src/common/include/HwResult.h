/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_HWRESULT_H
#define HARDWAREVIDEOCODEC_HWRESULT_H

#include "Object.h"

using namespace std;

class HwResult : public Object {
public:
    int code = 0;

    HwResult(int code) {
        this->code = code;
    }

    bool operator==(const HwResult &ret) const {
        return ret.code == code;
    }

    bool operator!=(const HwResult &ret) const {
        return ret.code != code;
    }
};
namespace Hw {
    const HwResult SUCCESS = HwResult(0);
    const HwResult FAILED = HwResult(-1);
}

#endif //HARDWAREVIDEOCODEC_HWRESULT_H
