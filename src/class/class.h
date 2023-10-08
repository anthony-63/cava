#pragma once

#include <inttypes.h>

#include "constant_pool.h"

typedef struct {
    uint32_t magic;
    uint16_t minor_version;
    uint16_t major_version;
    uint16_t constant_pool_count;
    CavaConstantPoolInfo* consatnt_pool;
    uint16_t access_flags;
    uint16_t this_class;
    uint16_t super_class;
    uint16_t interfaces_count;
    uint16_t* interfaces;
} CavaClassFile;

CavaClassFile read_class_file(const char* file_name);