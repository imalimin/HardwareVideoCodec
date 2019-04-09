/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_YUV420PFILTER_H
#define HARDWAREVIDEOCODEC_YUV420PFILTER_H

#include "Filter.h"

class YUV420PFilter : public Filter {
public:
    YUV420PFilter();

    ~YUV420PFilter();

    bool init(int w, int h) override;

    virtual void draw(GLuint yTexture, GLuint uTexture, GLuint vTexture);

    void bindResources() override;

private:
    GLuint uTexture = GL_NONE;
    GLuint vTexture = GL_NONE;
    GLint uLocation = GL_NONE;
    GLint vLocation = GL_NONE;
};


#endif //HARDWAREVIDEOCODEC_YUV420PFILTER_H
