/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "FilterReader.h"
#include "Base64.h"
#include "log.h"

FilterReader::FilterReader(char *path) {
    if (!doc.LoadFile(path)) {
        LOGE("Open file failed");
    }
}

FilterReader::~FilterReader() {
    doc.Clear();
}

FilterEntity *FilterReader::read() {
    LOGI(__func__);
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
        if (strcmp(elemName.c_str(), "vertex") == 0) {
            entity->vertex = string(elem->GetText());
            LOGI("vertex: %s", elem->GetText());
        } else if (strcmp(elemName.c_str(), "fragment") == 0) {
            entity->fragment = string(elem->GetText());
        } else if (strcmp(elemName.c_str(), "param") == 0) {
            string key = elem->Attribute("key");
            string value = elem->GetText();
            entity->params[key] = value;
        } else if (strcmp(elemName.c_str(), "sampler") == 0) {
            string key = elem->Attribute("key");
            string value = elem->FirstChild()->Value();
            entity->samplers[key] = base64_decode(value);
        }
    }
    return entity;
}