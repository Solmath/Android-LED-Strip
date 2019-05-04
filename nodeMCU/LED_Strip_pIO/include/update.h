#ifndef UPDATE_H
#define UPDATE_H

#include <ESP8266WebServer.h>
#include <ESP8266HTTPUpdateServer.h>;

void initUpdate(ESP8266WebServer *httpServer, ESP8266HTTPUpdateServer *httpUpdater, const char *hostname);
void handleOTA(void);

#endif // OTA_H