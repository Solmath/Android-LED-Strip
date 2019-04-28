#ifndef MACROS_H
#define MACROS_H


//debug macros
#ifdef DEBUG
 #define DEBUG_PRINT(x)  Serial.print (x)
 #define DEBUG_PRINTLN(x) Serial.println (x)
 #define DEBUG_PRINTF(x) Serial.printf (x)
 // unsigned long debugTime = 0;
 // int lastWifiState = 3;
 // unsigned long wifiStateChangedTime = 0;
#else
 #define DEBUG_PRINT(x)
 #define DEBUG_PRINTLN(x)
 #define DEBUG_PRINTF(x)
#endif

#endif /* MACROS_H */