/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/FileUtils.h"

unsigned long readFile(string path, uint8_t **buffer) {
    ifstream infile;
    infile.open(path.data());
    if (!infile.is_open()) return 0;
    return readStream(infile, buffer);
}

unsigned long readStream(ifstream &infile, uint8_t **buffer) {
    infile.seekg(0, ios::end);
    unsigned long length = static_cast<unsigned long>(infile.tellg());
    infile.seekg(0, ios::beg);
    *buffer = new uint8_t[length];
    infile.read(reinterpret_cast<char *>(*buffer), length);
    infile.close();
    return length;
}