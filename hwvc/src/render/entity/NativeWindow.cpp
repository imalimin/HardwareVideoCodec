//
// Created by mingyi.li on 2018/12/25.
//

#include "NativeWindow.h"

NativeWindow::NativeWindow(ANativeWindow *win) {
    this->win = win;
}

NativeWindow::~NativeWindow() {

}
