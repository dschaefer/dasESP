list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/cmake/Modules")

set(CMAKE_TRY_COMPILE_TARGET_TYPE STATIC_LIBRARY)

set(CMAKE_SYSTEM_NAME ESP8266)

set(gccPath ${CMAKE_CURRENT_LIST_DIR}/tools/xtensa-lx106-elf)

set(CMAKE_C_COMPILER ${gccPath}/bin/xtensa-lx106-elf-gcc)
set(CMAKE_CXX_COMPILER ${gccPath}/bin/xtensa-lx106-elf-g++)

set(CMAKE_C_FLAGS "-Os -g -Wpointer-arith -Wundef -Werror -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -ffunction-sections -fdata-sections -DICACHE_FLASH" CACHE STRING "C compiler flags")
set(CMAKE_CXX_FLAGS "-Os -g -Wpointer-arith -Wundef -Werror -Wl,-EL -fno-exceptions -fno-rtti -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -ffunction-sections -fdata-sections -DICACHE_FLASH" CACHE STRING "C++ compiler flags")
set(CMAKE_EXE_LINKER_FLAGS "-Wl,--gc-sections -nostdlib -T${CMAKE_CURRENT_LIST_DIR}/ld/eagle.app.v6.ld -Wl,--no-check-sections -u call_user_start -Wl,-static" CACHE STRING "linker flags")

set(CMAKE_C_LINK_EXECUTABLE "<CMAKE_C_COMPILER> <FLAGS> <CMAKE_C_LINK_FLAGS> <LINK_FLAGS> -Wl,--start-group <OBJECTS> <LINK_LIBRARIES> -Wl,--end-group -o <TARGET>" CACHE STRING "C linker invocation")
set(CMAKE_CXX_LINK_EXECUTABLE "<CMAKE_CXX_COMPILER> <FLAGS> <CMAKE_CXX_LINK_FLAGS> <LINK_FLAGS> -Wl,--start-group <OBJECTS> <LINK_LIBRARIES> -Wl,--end-group -o <TARGET>" CACHE STRING "CXX linker invocation")

include_directories(SYSTEM
	"${CMAKE_CURRENT_LIST_DIR}/include" 
	"${CMAKE_CURRENT_LIST_DIR}/extra_include"
	"${CMAKE_CURRENT_LIST_DIR}/include/espressif"
	"${CMAKE_CURRENT_LIST_DIR}/include/lwip"
	"${CMAKE_CURRENT_LIST_DIR}/include/lwip/ipv4"
	"${CMAKE_CURRENT_LIST_DIR}/include/lwip/ipv6"
	"${CMAKE_CURRENT_LIST_DIR}/nopoll"
	"${CMAKE_CURRENT_LIST_DIR}/include/spiffs"
	"${CMAKE_CURRENT_LIST_DIR}/include/ssl"
	"${CMAKE_CURRENT_LIST_DIR}/include/json" 
)

link_directories("${CMAKE_CURRENT_LIST_DIR}/lib")

link_libraries(
	-lcirom
	-lcrypto
	-lespconn
	-lespnow
	-lfreertos
	-lgcc
	-lhal
	-ljson
	-llwip
	-lmain
	-lmesh
	-lmirom
	-lnet80211
	-lnopoll
	-lphy
	-lpp
	-lpwm
	-lsmartconfig
	-lspiffs
	-lssl
	-lwpa
	-lwps
)
