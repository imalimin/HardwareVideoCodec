/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_STRINGUTILS_H
#define HARDWAREVIDEOCODEC_STRINGUTILS_H

#include <string.h>
#include <iostream>
#include <algorithm>
#include <string>
#include <vector>

class StringUtils {
public:

    static std::string trimLeft(const std::string &str, const std::string &token = " ") {
        std::string t = str;
        t.erase(0, t.find_first_not_of(token));
        return t;
    }

    static std::string trimRight(const std::string &str, const std::string &token = " ") {
        std::string t = str;
        t.erase(t.find_last_not_of(token) + 1);
        return t;
    }

    static std::string trim(const std::string &str, const std::string &token = " ") {
        std::string t = str;
        t.erase(0, t.find_first_not_of(token));
        t.erase(t.find_last_not_of(token) + 1);
        return t;
    }

    static std::string toLower(const std::string &str) {
        std::string t = str;
        std::transform(t.begin(), t.end(), t.begin(), tolower);
        return t;
    }

    static std::string toUpper(const std::string &str) {
        std::string t = str;
        std::transform(t.begin(), t.end(), t.begin(), toupper);
        return t;
    }

    static bool startsWith(const std::string &str, const std::string &substr) {
        return str.find(substr) == 0;
    }

    static bool endsWith(const std::string &str, const std::string &substr) {
        return str.rfind(substr) == (str.length() - substr.length());
    }

    static bool equalsIgnoreCase(const std::string &str1, const std::string &str2) {
        return toLower(str1) == toLower(str2);
    }

    static std::vector<std::string> split(const std::string &str, const std::string &delimiter) {
        char *save = nullptr;
        char *token = strtok_r(const_cast<char *>(str.c_str()), delimiter.c_str(), &save);
        std::vector<std::string> result;
        while (token != nullptr) {
            result.emplace_back(token);
            token = strtok_r(nullptr, delimiter.c_str(), &save);
        }
        return result;
    }
};


#endif //HARDWAREVIDEOCODEC_STRINGUTILS_H
