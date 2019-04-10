//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_PICTUREPROCESSOR_H
#define HARDWAREVIDEOCODEC_PICTUREPROCESSOR_H

#include "Object.h"
#include "UnitPipeline.h"
#include "Screen.h"
#include "Filter.h"
#include "HwWindow.h"

class PictureProcessor : public Object {
public:
    PictureProcessor();

    virtual ~PictureProcessor();

    void prepare(HwWindow *win, int width, int height);

    void show(char *file);

    void setFilter(Filter *filter);

    void invalidate();

private:
    UnitPipeline *pipeline = nullptr;
};


#endif //HARDWAREVIDEOCODEC_PICTUREPROCESSOR_H
