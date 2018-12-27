//
// Created by mingyi.li on 2018/12/27.
//

#ifndef HARDWAREVIDEOCODEC_TEXTURECENTR_H
#define HARDWAREVIDEOCODEC_TEXTURECENTR_H

#include <vector>
#include "Object.h"
#include <GLES2/gl2.h>

using namespace std;

class TextureCenter : public Object {
public:
    TextureCenter();

    virtual ~TextureCenter();

    GLuint alloc();

    GLuint alloc(uint8_t *rgba, int width, int height);

    void recycle(GLuint texture);

private:
    vector<GLuint> textures;

    void clear();
};


#endif //HARDWAREVIDEOCODEC_TEXTURECENTR_H
