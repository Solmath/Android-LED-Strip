#ifndef COLOR_H
#define COLOR_H

// #include <stdint.h>

typedef struct {
  float r;
  float g;
  float b;
} RGB_color_t;

typedef struct {
  float h;
  float s;
  float v;
} HSV_color_t;

HSV_color_t   rgb2hsv(RGB_color_t in);
RGB_color_t   hsv2rgb(HSV_color_t in);

#endif /* COLOR_H */