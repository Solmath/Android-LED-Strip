#ifndef JSON_PARSER_H
#define JSON_PARSER_H

extern "C" {
#include "color.h"
}

void parseJsonPacket(char *packetBuffer, int *mode, RGB_color_t *newColor);

#endif /* JSON_PARSER_H */