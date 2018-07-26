//
// Created by limin on 2018/7/26.
//

#include "SpecificData.h"

SpecificData::SpecificData(char *data, int size) {
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
