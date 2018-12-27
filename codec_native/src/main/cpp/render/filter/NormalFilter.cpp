//
// Created by mingyi.li on 2018/12/27.
//

#include "../include/NormalFilter.h"
#include "../include/NormalDrawer.h"

NormalFilter::NormalFilter(int w, int h) : Filter(w, h) {
    name = __func__;
    drawer = new NormalDrawer();
}

NormalFilter::~NormalFilter() {

}