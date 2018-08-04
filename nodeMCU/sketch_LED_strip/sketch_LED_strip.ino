#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library
#include <WiFiUdp.h>

#include <DNSServer.h>            //Local DNS Server used for redirecting all requests to the configuration portal
#include <ESP8266WebServer.h>     //Local WebServer used to serve the configuration portal
#include <WiFiManager.h>          //https://github.com/tzapu/WiFiManager WiFi Configuration Magic

unsigned int port = 2390;

char packetBuffer[255];
char ReplyBuffer[] = "acknowledged";

WiFiUDP Udp;

const int LED_RED = D6;
const int LED_GREEN = D5;
const int LED_BLUE = D7;

// const int LED_BUILTIN_2 = D4;

void setup()
{
  // set pin modes
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);

  // pinMode(LED_BUILTIN, OUTPUT);
  // pinMode(LED_BUILTIN_2, OUTPUT);

  digitalWrite(LED_RED, HIGH);
  delay(300);
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_GREEN, HIGH);
  delay(300);
  digitalWrite(LED_GREEN, LOW);
  digitalWrite(LED_BLUE, HIGH);
  delay(300);
  digitalWrite(LED_BLUE, LOW);

  // begin serial and connect to WiFi
  Serial.begin(115200);
  delay(100);

  Serial.println("");
  Serial.println("Connecting to wifi");
  Serial.println("");
  
  WiFiManager wifiManager;
  wifiManager.autoConnect();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("");

  Udp.begin(port);
}

void loop()
{
  int packetSize = Udp.parsePacket();
  char buf[100];
  
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

    int r = 0;
    int g = 0;
    int b = 0;

    temp = strtok (packetBuffer,":");
    r = atoi(temp);

    if(temp != NULL){
      temp = strtok (NULL,":");
      g = atoi(temp);
    }

    if(temp != NULL){
      temp = strtok (NULL,":");
      b = atoi(temp);
    }

    sprintf(buf,"r = %4d\ng = %4d\nb = %4d",r, g, b);

    Serial.println(buf);
    Serial.println("");

    analogWrite(LED_RED, r);
    analogWrite(LED_GREEN, g);
    analogWrite(LED_BLUE, b);

    // debugging with onboard LEDs
    // analogWrite(LED_BUILTIN, 1023-b);
    // analogWrite(LED_BUILTIN_2, 1023-g);

    // send a reply, to the IP address and port
    // that sent us the packet we received
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();
  }
}
