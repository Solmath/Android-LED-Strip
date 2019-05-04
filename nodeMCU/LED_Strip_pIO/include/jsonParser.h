#ifndef JSONPARSER_H
#define JSONPARSER_H

extern "C" {
#include "color.h"
}

void parseJsonPacket(char *packetBuffer, int *mode, RGB_color_t *newColor);

#endif /* JSONPARSER_H */