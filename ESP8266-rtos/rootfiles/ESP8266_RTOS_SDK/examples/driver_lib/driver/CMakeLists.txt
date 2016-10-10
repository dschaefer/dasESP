add_library(esp8266_drivers gpio.c hw_timer.c uart.c)
target_include_directories(esp8266_drivers PUBLIC ${ESP8266_HOME}/examples/driver_lib/include)
