//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_PICTUREPROCESSOR_H
#define HARDWAREVIDEOCODEC_PICTUREPROCESSOR_H

#include "Object.h"
#include "MainPipeline.h"
#include "Screen.h"

class PictureProcessor : public Object {
public:
    PictureProcessor();

    virtual ~PictureProcessor();

    void prepare(ANativeWindow *win, int width, int height);

    void show(uint8_t *rgba, int width, int height);

private:
    MainPipeline *pipeline = nullptr;
};


#endif //HARDWAREVIDEOCODEC_PICTUREPROCESSOR_H
