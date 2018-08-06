/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <string.h>

#ifndef HARDWAREVIDEOCODEC_SPECIFICDATA_H
#define HARDWAREVIDEOCODEC_SPECIFICDATA_H


class SpecificData {
public:
    SpecificData(const char *data, int size);

    char *get();

    int size();

    bool alreadySent();

    void setSent(bool sent);

    ~SpecificData();

private:
    char *data = NULL;
    int s;
    bool sent = false;
};


#endif //HARDWAREVIDEOCODEC_SPECIFICDATA_H
