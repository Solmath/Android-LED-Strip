#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library
#include <WiFiUdp.h>

#include <DNSServer.h>            //Local DNS Server used for redirecting all requests to the configuration portal
#include <ESP8266WebServer.h>     //Local WebServer used to serve the configuration portal
#include <WiFiManager.h>          //https://github.com/tzapu/WiFiManager WiFi Configuration Magic

#include <EEPROM.h>

extern "C" {
#include "user_interface.h"
}

#define DEBUG

unsigned int port = 2390;

char packetBuffer[255];
char ReplyBuffer[] = "acknowledged";

WiFiUDP Udp;

const int LED_RED = D6;
const int LED_GREEN = D5;
const int LED_BLUE = D7;

// const int LED_BUILTIN_2 = D4;

os_timer_t eepromTimer;
bool timerEvent = false;

typedef struct {
  int16_t r;
  int16_t g;
  int16_t b;
} color_t;

void eepromTimerCallback(void *pArg)
{
  timerEvent = true;
}

void setup()
{
  char buf[100];
  color_t oldColor;
  
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
  EEPROM.get(0, oldColor);
  EEPROM.end();

  analogWrite(LED_RED, oldColor.r);
  analogWrite(LED_GREEN, oldColor.g);
  analogWrite(LED_BLUE, oldColor.b);
  
  // begin serial and connect to WiFi
  Serial.begin(115200);
  delay(100);

  Serial.println("----------------------------");
  Serial.println("Connecting to wifi");
  Serial.println("----------------------------");
  
  WiFiManager wifiManager;
  wifiManager.autoConnect();

  Serial.println("----------------------------");
  Serial.println("WiFi connected");
  Serial.println("----------------------------");

  Udp.begin(port);

  os_timer_setfn(&eepromTimer, eepromTimerCallback, NULL);
}

void loop()
{
  int packetSize = Udp.parsePacket();
  char buf[100];

  static color_t newColor = {0};
  
  if(packetSize) {
    Serial.print("Received packet of size ");
    Serial.println(packetSize);
    Serial.print("From ");
    IPAddress remoteIp = Udp.remoteIP();
    Serial.print(remoteIp);
    Serial.print(", port ");
    Serial.println(Udp.remotePort());

    // read the packet into packetBufffer
    int len = Udp.read(packetBuffer, 255);
    if (len > 0) {
      packetBuffer[len] = 0;
    }
    
    Serial.println("Contents:");
    Serial.println(packetBuffer);
    Serial.println("");

    // TEMP parse data from packet
    char * temp;

    newColor.r = 0;
    newColor.g = 0;
    newColor.b = 0;

    temp = strtok (packetBuffer,":");
    newColor.r = atoi(temp);

    if(temp != NULL){
      temp = strtok (NULL,":");
      newColor.g = atoi(temp);
    }

    if(temp != NULL){
      temp = strtok (NULL,":");
      newColor.b = atoi(temp);
    }

    sprintf(buf,"r = %4d\ng = %4d\nb = %4d", newColor.r, newColor.g, newColor.b);

    Serial.println(buf);
    Serial.println("");

    analogWrite(LED_RED, newColor.r);
    analogWrite(LED_GREEN, newColor.g);
    analogWrite(LED_BLUE, newColor.b);

    // debugging with onboard LEDs
    // analogWrite(LED_BUILTIN, 1023-b);
    // analogWrite(LED_BUILTIN_2, 1023-g);

    // send a reply, to the IP address and port
    // that sent us the packet we received
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();

    os_timer_arm(&eepromTimer, 30000, false); // write eeprom 10s after last message
  }

  if (timerEvent)
  {
    // Update eeprom
    #ifndef DEBUG
    EEPROM.begin(6);
    EEPROM.put( 0, newColor );         // don't write during debugging to reduce wear on eeprom 
    EEPROM.end();                      // Free RAM copy of structure
    #endif

    Serial.println("----------------------------");
    Serial.println("Value written to eeprom.");
    Serial.println("----------------------------");
    
    timerEvent = false;
  }
}
