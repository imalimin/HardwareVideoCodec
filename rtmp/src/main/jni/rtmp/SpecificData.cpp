/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <malloc.h>
#include "SpecificData.h"

SpecificData::SpecificData(const char *data, int size) {
    this->s = size;
    if (NULL != this->data) {
        free(this->data);
    }
    this->data = (char *) malloc(sizeof(char) * this->s);
    memcpy(this->data, data, this->s);
}

char *SpecificData::get() {
    return data;
}

int SpecificData::size() {
    return s;
}

bool SpecificData::alreadySent() {
    return sent;
}

void SpecificData::setSent(bool sent) {
    this->sent = sent;
}

SpecificData::~SpecificData() {
    if (this->data) {
        free(this->data);
    }
}
