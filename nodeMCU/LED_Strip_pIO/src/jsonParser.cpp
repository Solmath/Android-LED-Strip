#include "jsonParser.h"

#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library
#include <ArduinoJson.h>

void parseJsonPacket(char *packetBuffer, int *mode, RGB_color_t *newColor)
{
    StaticJsonDocument<200> jsonDoc;
    DeserializationError error = deserializeJson(jsonDoc, packetBuffer);
    if (error) {
        Serial.print(F("deserializeJson() failed with code "));
        Serial.println(error.c_str());
        return;
    }
    // JsonObject root = jsonDoc.to<JsonObject>();

    HSV_color_t HSVcolor;

    bool setHue = jsonDoc["setHue"];
    if (setHue)
    {
        HSVcolor.h = jsonDoc["hue"];
    }

    bool setSaturation = jsonDoc["setSaturation"];
    if (setSaturation)
    {
        HSVcolor.s = jsonDoc["saturation"];
    }

    bool setBrightness = jsonDoc["setBrightness"];
    if (setBrightness)
    {
        HSVcolor.v = jsonDoc["brightness"];
    }
    *mode = jsonDoc["mode"];

    char buf[100];

    sprintf(buf,"H = %4f\nS = %4f\nV = %4f", HSVcolor.h, HSVcolor.s, HSVcolor.v);

    Serial.println(buf);
    Serial.println("");

    *newColor = hsv2rgb(HSVcolor);

    // debugging with onboard LEDs
    // analogWrite(LED_BUILTIN, 1023-b);
    // analogWrite(LED_BUILTIN_2, 1023-g);
}