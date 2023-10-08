#pragma once

#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <string.h>

typedef struct {
    char* file_name;
    size_t bytes_length;
    uint32_t index;
    uint8_t* bytes;
} CavaClassReader;

CavaClassReader* class_reader_init(const char* file_name);
uint8_t class_reader_get_u8(CavaClassReader* reader);
uint16_t class_reader_get_u16(CavaClassReader* reader);
uint32_t class_reader_get_u32(CavaClassReader* reader);