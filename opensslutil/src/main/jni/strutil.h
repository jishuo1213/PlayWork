//
// Created by fan on 16-7-12.
//

#ifndef DECRYPTMAIL_STRUTIL_H
#define DECRYPTMAIL_STRUTIL_H

#include <android/log.h>

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "encrypt_mail::", __VA_ARGS__))

int strToHex(unsigned char *ch, int length, char *hex);

int hexToStr(char *hex, char *ch);

int hexCharToValue(const char ch);

char valueToHexCh(const int value);

#endif //DECRYPTMAIL_STRUTIL_H
