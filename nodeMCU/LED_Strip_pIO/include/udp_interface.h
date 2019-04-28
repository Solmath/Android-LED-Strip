#ifndef UDP_INTERFACE_H
#define UDP_INTERFACE_H

#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library
#include <WiFiUdp.h>

#define UDP_LEN 255

void handleUdpPacket(WiFiUDP *Udp, char *packetBuffer);

#endif /* UDP_INTERFACE_H */