/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_AUDIOPROCESSOR_H
#define HARDWAREVIDEOCODEC_AUDIOPROCESSOR_H

#include "Object.h"
#include <string>
#include "UnitPipeline.h"

using namespace std;

class AudioProcessor : public Object {
public:
    AudioProcessor();

    virtual ~AudioProcessor();

    void setSource(const string *path);

    void prepare();

    void start();

    void pause();

    void stop();

    void seek(int64_t us);

private:
    UnitPipeline *pipeline = nullptr;
};


#endif //HARDWAREVIDEOCODEC_AUDIOPROCESSOR_H
