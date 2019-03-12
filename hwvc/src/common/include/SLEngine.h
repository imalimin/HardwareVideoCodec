/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_ENGINE_H
#define HARDWAREVIDEOCODEC_ENGINE_H

#include "Object.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

class SLEngine : public Object {
public:
    SLEngine();

    ~SLEngine();

    virtual bool valid();

    virtual SLEngineItf getEngine() {
        return engineItf;
    }

private:
    SLObjectItf engineObject = nullptr;
    SLEngineItf engineItf = nullptr;

    void release();

};


#endif //HARDWAREVIDEOCODEC_ENGINE_H
