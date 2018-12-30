//
// Created by limin on 2018/12/30.
//

#ifndef HARDWAREVIDEOCODEC_BASE64_H
#define HARDWAREVIDEOCODEC_BASE64_H

#include <string>

using namespace std;

std::string base64_encode(unsigned char const* , unsigned int len);
std::string base64_decode(std::string const& s);


#endif //HARDWAREVIDEOCODEC_BASE64_H
