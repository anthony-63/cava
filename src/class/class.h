#pragma once

#include <inttypes.h>

#include "constant_pool.h"

typedef struct {
    uint32_t magic;
    uint16_t minor_version;
    uint16_t major_version;
    uint16_t constant_pool_count;
    CavaConstantPoolInfo* constant_pool;
    uint16_t access_flags;
    uint16_t this_class;
    uint16_t super_class;
    uint16_t interfaces_count;
    uint16_t* interfaces;
} CavaClassFile;

void load_classpath(CavaClassFile* dest, char* classpath, int* count);
void get_classfile_dependencies(char** dest, CavaClassFile file);
char* resolve_utf8(CavaClassFile file, int index);
CavaClassFile read_class_file(const char* file_name);
void read_constant_pool(CavaClassFile* class, CavaClassReader* reader);
