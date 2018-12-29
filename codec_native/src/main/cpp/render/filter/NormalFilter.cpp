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

void NormalFilter::init(int w, int h) {
    drawer = new NormalDrawer();
}