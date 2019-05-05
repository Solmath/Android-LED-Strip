#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library
#include <ESP8266SSDP.h>
#include <ESP8266WebServer.h>     //Local WebServer used to serve the configuration portal
#include <ESP8266mDNS.h>

#include <DNSServer.h>            //Local DNS Server used for redirecting all requests to the configuration portal
#include <WiFiManager.h>          //https://github.com/tzapu/WiFiManager WiFi Configuration Magic
#include <WiFiUdp.h>

#include <EEPROM.h>
#include "SSDPDevice.h"

#include "udpInterface.h"
#include "jsonParser.h"
#include "timers.h"
#include "update.h"

extern "C" {
#include "global.h"
// #include "user_interface.h"
#include "color.h"
}

WiFiUDP Udp;

ESP8266WebServer httpServer(80);
ESP8266HTTPUpdateServer httpUpdater;

unsigned int port = 2390; // port for UDP connection
const char* host = "esp8266-webupdate";

const int LED_RED = D6;
const int LED_GREEN = D5;
const int LED_BLUE = D7;

const int STATIC_COLOR = 1;
const int ANIMATION = 2;

// const int LED_BUILTIN_2 = D4;

void setup()
{
  // TODO: DNS-Server, damit Seite zur Wifi-Auswahl über URL erreichbar ist
  // TODO: FOTA
  // TODO: erweitertes JSON-Objekt analog zu WLED (evtl. als eigene Klasse)
  RGB_color_t lastColor;
  
  // set pin modes
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);

  // pinMode(LED_BUILTIN, OUTPUT);
  // pinMode(LED_BUILTIN_2, OUTPUT);

  // flash colors to signal init
  digitalWrite(LED_RED, HIGH);
  delay(300);
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_GREEN, HIGH);
  delay(300);
  digitalWrite(LED_GREEN, LOW);
  digitalWrite(LED_BLUE, HIGH);
  delay(300);
  digitalWrite(LED_BLUE, LOW);

  // restore last color from eeprom
  EEPROM.begin(6);
  EEPROM.get(0, lastColor);
  EEPROM.end();

  analogWrite(LED_RED, lastColor.r);
  analogWrite(LED_GREEN, lastColor.g);
  analogWrite(LED_BLUE, lastColor.b);
  
  // begin serial and connect to WiFi
  Serial.begin(115200);
  while (!Serial) continue;

  Serial.println("----------------------------");
  Serial.println("Connecting to wifi");
  Serial.println("----------------------------");
  
  WiFiManager wifiManager;
  wifiManager.autoConnect();

  Serial.println("----------------------------");
  Serial.println("WiFi connected");
  Serial.println("----------------------------");

  initUpdate(&httpServer, &httpUpdater, host);

  Serial.printf("Starting HTTP...\n");

  httpServer.on("/", HTTP_GET, []() {
    httpServer.send(200, "text/html", "<html><body>Hello world! <br><br> Firmware updates can be uploaded <a href=\"/update\">here</a>.</body></html>");
  });

   httpServer.on("/reset", HTTP_GET, []() {
    httpServer.send(200, "text/plain", "Resetting...");
    delay(3000);
    ESP.restart(); 
  });
  
  httpServer.on("/description.xml", HTTP_GET, [](){
    SSDPDevice.schema( httpServer.client());     
  });

  SSDPDevice.setName("SSDP Test");
  SSDPDevice.setDeviceType("urn:schemas-upnp-org:device:BinaryLight:1");
  SSDPDevice.setSchemaURL("description.xml");
  SSDPDevice.setSerialNumber(ESP.getChipId());
  SSDPDevice.setURL("/");
  SSDPDevice.setModelName("ESP8266 Home Control");
  SSDPDevice.setModelNumber("1");
  SSDPDevice.setManufacturer("Solmath");
  // SSDPDevice.setManufacturerURL("http://www.peut.org/");

  httpServer.begin();

  MDNS.addService("http", "tcp", 80);

  Udp.begin(port);

  setupEepromTimer();
}

void loop()
{

  int mode;
  RGB_color_t newColor;
  int packetSize = Udp.parsePacket();

  handleOTA();
  httpServer.handleClient();
  SSDPDevice.handleClient();
  
  if(packetSize) {
    char packetBuffer[UDP_LEN];
    handleUdpPacket(&Udp, packetSize, packetBuffer);
    parseJsonPacket(packetBuffer, &mode, &newColor);
  }

  if (mode == STATIC_COLOR)
  {
    analogWrite(LED_RED, (int)(newColor.r * 1023.0));
    analogWrite(LED_GREEN, (int)(newColor.g * 1023.0));
    analogWrite(LED_BLUE, (int)(newColor.b * 1023.0));
  }
  else if (mode == ANIMATION)
  {
    /*
    if (animationTimerEvent)
    {
      animationTimerEvent = false;
    }
    */
  }

  if (getEepromTimerEvent())
  {
    // TODO: Maybe use SPIFFS (see diyHue)
    // Update eeprom
    #ifndef DEBUG
    EEPROM.begin(6);
    EEPROM.put( 0, newColor );         // don't write during debugging to reduce wear on eeprom 
    EEPROM.end();                      // Free RAM copy of structure
    #endif

    Serial.println("----------------------------");
    Serial.println("Value written to eeprom.");
    Serial.println("----------------------------");
    
    resetEepromTimerEvent();
  }
}
