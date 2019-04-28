#include "json_parser.h"

#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library
#include <ArduinoJson.h>

void parseJsonPacket(char *packetBuffer, int *mode, RGB_color_t *newColor)
{
    StaticJsonBuffer<200> jsonBuffer;
    JsonObject& root = jsonBuffer.parseObject(packetBuffer);

    HSV_color_t HSVcolor;

    bool setHue = root["setHue"];
    if (setHue)
    {
        HSVcolor.h = root["hue"];
    }

    bool setSaturation = root["setSaturation"];
    if (setSaturation)
    {
        HSVcolor.s = root["saturation"];
    }

    bool setBrightness = root["setBrightness"];
    if (setBrightness)
    {
        HSVcolor.v = root["brightness"];
    }
    *mode = root["mode"];

    char buf[100];

    sprintf(buf,"H = %4f\nS = %4f\nV = %4f", HSVcolor.h, HSVcolor.s, HSVcolor.v);

    Serial.println(buf);
    Serial.println("");

    *newColor = hsv2rgb(HSVcolor);

    // debugging with onboard LEDs
    // analogWrite(LED_BUILTIN, 1023-b);
    // analogWrite(LED_BUILTIN_2, 1023-g);
}