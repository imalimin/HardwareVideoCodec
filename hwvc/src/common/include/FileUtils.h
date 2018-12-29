/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_FILEUTILS_H
#define HARDWAREVIDEOCODEC_FILEUTILS_H

#include <string>
#include <fstream>

using namespace std;

unsigned long readFile(string file, uint8_t **buffer);

unsigned long readStream(ifstream &infile, uint8_t **buffer);

class FileUtils {

};


#endif //HARDWAREVIDEOCODEC_FILEUTILS_H
