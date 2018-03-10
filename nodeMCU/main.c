#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

const char* ssid      = "ssid";
const char* password  = "password";

unsigned int port = 2390;

char packetBuffer[255];
char ReplyBuffer[] = "acknowledged";

WiFiUDP Udp;

const int REDPIN = 12;
const int GREENPIN = 16;
const int BLUEPIN = 13;

void setup()
{
  // set pin modes
  pinMode(REDPIN, OUTPUT);
  pinMode(GREENPIN, OUTPUT);
  pinMode(BLUEPIN, OUTPUT);
  pinMode(ledPin, OUTPUT);

  digitalWrite(REDPIN, HIGH);
  delay(300);
  digitalWrite(REDPIN, LOW);
  digitalWrite(GREENPIN, HIGH);
  delay(300);
  digitalWrite(GREENPIN, LOW);
  digitalWrite(BLUEPIN, HIGH);
  delay(300);
  digitalWrite(BLUEPIN, LOW);

  // begin serial and connect to WiFi
  Serial.begin(115200);
  delay(100);

  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  Udp.begin(port);

}



int value = 0;

void loop()
{
  int packetSize = Udp.parsePacket();
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
    Serial.write(packetBuffer);
    Serial.println();

    // TEMP parse data from packet
    char * temp;
    Serial.print("Splitting string \"");
    Serial.print(packetBuffer);
    Serial.println("\"");

    int r = 0;
    int b = 0;
    int g = 0;

    temp = strtok (packetBuffer,":");
    Serial.println(temp);
    r = atoi(temp);

    if(temp != NULL){
      temp = strtok (NULL,":");
      Serial.println(temp);
      g = atoi(temp);
    }

    if(temp != NULL){
      temp = strtok (NULL,":");
      Serial.println(temp);
      b = atoi(temp);
    }

    Serial.println();
    Serial.println(r);
    Serial.println(g);
    Serial.println(b);

    analogWrite(REDPIN, r);
    analogWrite(GREENPIN, g);
    analogWrite(BLUEPIN, b);

    // send a reply, to the IP address and port
    // that sent us the packet we received
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();

  }


}