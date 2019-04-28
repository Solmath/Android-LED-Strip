#ifndef TIMERS_H
#define TIMERS_H

#define EEPROM_DELAY_MS 30000

bool getEepromTimerEvent(void);
void resetEepromTimerEvent(void);
void setupEepromTimer(void);
void resetEepromTimer(void);

#endif /* TIMERS_H */