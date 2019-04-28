#include "timers.h"

#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library

os_timer_t eepromTimer;

bool eepromTimerEvent = false;
bool animationTimerEvent = false;

bool getEepromTimerEvent(void)
{
    return eepromTimerEvent;
}

void resetEepromTimerEvent(void)
{
    eepromTimerEvent = false;
}

void eepromTimerCallback(void *pArg)
{
  eepromTimerEvent = true;
}

void animationTimerCallback(void *pArg)
{
  animationTimerEvent = true;
}

void setupEepromTimer(void)
{
     os_timer_setfn(&eepromTimer, eepromTimerCallback, NULL);
}

void resetEepromTimer(void)
{
    os_timer_arm(&eepromTimer, EEPROM_DELAY_MS, false); 
}