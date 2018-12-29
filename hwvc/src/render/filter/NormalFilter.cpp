//
// Created by mingyi.li on 2018/12/27.
//

#include "../include/NormalFilter.h"
#include "../include/NormalDrawer.h"

NormalFilter::NormalFilter() {
    name = __func__;
}

NormalFilter::~NormalFilter() {

}

bool NormalFilter::init(int w, int h) {
    if (!Filter::init(w, h))
        return false;
    drawer = new NormalDrawer();
    return true;
}