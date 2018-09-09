#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library
#include <WiFiUdp.h>

#include <DNSServer.h>            //Local DNS Server used for redirecting all requests to the configuration portal
#include <ESP8266WebServer.h>     //Local WebServer used to serve the configuration portal
#include <WiFiManager.h>          //https://github.com/tzapu/WiFiManager WiFi Configuration Magic

#include <EEPROM.h>

#include <ArduinoJson.h>

extern "C" {
// #include "user_interface.h"
#include "color.h"
}

#define DEBUG

unsigned int port = 2390;

char ReplyBuffer[] = "acknowledged";
char buf[100];

WiFiUDP Udp;

const int LED_RED = D6;
const int LED_GREEN = D5;
const int LED_BLUE = D7;

const int STATIC_COLOR = 1;
const int ANIMATION = 2;

// const int LED_BUILTIN_2 = D4;

os_timer_t eepromTimer;
bool eepromTimerEvent = false;
bool animationTimerEvent = false;

float hue;
bool setHue;
float saturation;
bool setSaturation;
float brightness;
bool setBrightness;
int mode;

HSV_color_t HSVcolor;

void eepromTimerCallback(void *pArg)
{
  eepromTimerEvent = true;
}

void animationTimerCallback(void *pArg)
{
  animationTimerEvent = true;
}

void parsePacket()
{
  char packetBuffer[255];
  StaticJsonBuffer<200> jsonBuffer;

  // read the packet into packetBufffer
  int len = Udp.read(packetBuffer, 255);
  if (len > 0) {
    packetBuffer[len] = 0;
  }
  
  Serial.println("Contents:");
  Serial.println(packetBuffer);
  Serial.println("");

  JsonObject& root = jsonBuffer.parseObject(packetBuffer);

  setHue = root["setHue"];
  if (setHue)
  {
    HSVcolor.h = root["hue"];
  }
  setSaturation = root["setSaturation"];
  if (setSaturation)
  {
    HSVcolor.s = root["saturation"];
  }
  setBrightness = root["setBrightness"];
  if (setBrightness)
  {
    HSVcolor.v = root["brightness"];
  }
  mode = root["mode"];

  sprintf(buf,"H = %4f\nS = %4f\nV = %4f", HSVcolor.h, HSVcolor.s, HSVcolor.v);

  Serial.println(buf);
  Serial.println("");
}

void setup()
{
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

  Udp.begin(port);

  os_timer_setfn(&eepromTimer, eepromTimerCallback, NULL);
}

void loop()
{
  int packetSize = Udp.parsePacket();
  RGB_color_t newColor;
  
  if(packetSize) {
    
    Serial.print("Received packet of size ");
    Serial.println(packetSize);
    Serial.print("From ");
    IPAddress remoteIp = Udp.remoteIP();
    Serial.print(remoteIp);
    Serial.print(", port ");
    Serial.println(Udp.remotePort());
    
    parsePacket();

    newColor = hsv2rgb(HSVcolor);
    
    // debugging with onboard LEDs
    // analogWrite(LED_BUILTIN, 1023-b);
    // analogWrite(LED_BUILTIN_2, 1023-g);

    // send a reply, to the IP address and port
    // that sent us the packet we received
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();

    os_timer_arm(&eepromTimer, 30000, false); // write eeprom 30s after last message
  }

  if (mode == STATIC_COLOR)
  {
    analogWrite(LED_RED, (int)(newColor.r * 1023.0));
    analogWrite(LED_GREEN, (int)(newColor.g * 1023.0));
    analogWrite(LED_BLUE, (int)(newColor.b * 1023.0));
  }
  else if (mode == ANIMATION)
  {
    if (animationTimerEvent)
    {
      animationTimerEvent = false;
    }
  }

  if (eepromTimerEvent)
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
    
    eepromTimerEvent = false;
  }
}
