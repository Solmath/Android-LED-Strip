#include "udpInterface.h"

#include "timers.h"

extern "C" {
#include "color.h"
}

void handleUdpPacket(WiFiUDP *Udp, char *packetBuffer)
{
    int packetSize = Udp->parsePacket();
  
    if(packetSize) {
    
        Serial.print("Received packet of size ");
        Serial.println(packetSize);
        Serial.print("From ");
        Serial.print(Udp->remoteIP());
        Serial.print(", port ");
        Serial.println(Udp->remotePort());
        
        // read the packet into packetBufffer
        int len = Udp->read(packetBuffer, UDP_LEN);
        if (len > 0) {
            packetBuffer[len] = 0;
        }
        
        Serial.println("Contents:");
        Serial.println(packetBuffer);
        Serial.println("");
        
        // send a reply, to the IP address and port
        // that sent us the packet we received
        char ReplyBuffer[] = "acknowledged";

        Udp->beginPacket(Udp->remoteIP(), Udp->remotePort());
        Udp->write(ReplyBuffer);
        Udp->endPacket();

        resetEepromTimer(); // write eeprom 30s after last message
    }
}