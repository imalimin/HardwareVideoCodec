/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <iostream>
#include "FilterReader.h"
#include "Base64.h"
#include "log.h"

using namespace std;

static const char *ATTR_KEY = "key";
static const char *VERSION = "version";
static const char *NAME = "name";
static const char *VERTEX = "vertex";
static const char *FRAGMENT = "fragment";
static const char *PARAM = "param";
static const char *SAMPLER = "sampler";

FilterReader::FilterReader(char *path) {
    if (!doc.LoadFile(path)) {
        LOGE("HVF open file failed");
    }
}

FilterReader::~FilterReader() {
    doc.Clear();
}

FilterEntity *FilterReader::read() {
    TiXmlElement *root = doc.FirstChildElement();
    if (NULL == root) {
        LOGE("Read xml file failed");
        doc.Clear();
        return nullptr;
    }
    string rootName = root->Value();
    if (0 != strcmp(rootName.c_str(), "hwvc_filter")) {
        LOGE("This xml is not a hwvc filter");
        doc.Clear();
        return nullptr;
    }
    FilterEntity *entity = new FilterEntity();
    for (TiXmlElement *elem = root->FirstChildElement();
         elem != NULL; elem = elem->NextSiblingElement()) {
        string elemName = elem->Value();
        if (strcmp(elemName.c_str(), VERSION) == 0) {
            entity->version = atoi(elem->GetText());
        } else if (strcmp(elemName.c_str(), NAME) == 0) {
            entity->name = string(elem->GetText());
            LOGI("name: %s", elem->GetText());
        } else if (strcmp(elemName.c_str(), VERTEX) == 0) {
            entity->vertex = string(elem->FirstChild()->Value());
        } else if (strcmp(elemName.c_str(), FRAGMENT) == 0) {
            entity->fragment = string(elem->FirstChild()->Value());
        } else if (strcmp(elemName.c_str(), PARAM) == 0) {
            string key = elem->Attribute(ATTR_KEY);
            string value = elem->GetText();
            entity->params[key] = static_cast<float>(atof(value.c_str()));
        } else if (strcmp(elemName.c_str(), SAMPLER) == 0) {
            string key = elem->Attribute(ATTR_KEY);
            string value = elem->FirstChild()->Value();
            entity->samplers[key] = base64_decode(value);
        }
    }
    return entity;
}