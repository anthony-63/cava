#pragma once

#include <inttypes.h>

typedef struct {
    uint8_t tag;
    uint16_t name_index;
} CavaConstantPoolClassInfo;

typedef struct {
    uint8_t tag;
    uint16_t class_index;
    uint16_t name_and_type_index;
} CavaConstantPoolFieldRefInfo;

typedef struct {
    uint8_t tag;
    uint16_t class_index;
    uint16_t name_and_type_index;
} CavaConstantPoolMethodRefInfo;

typedef struct {
    uint8_t tag;
    uint16_t class_index;
    uint16_t name_and_type_index;
} CavaConstantPoolInterfaceMethodRefInfo;

typedef struct {
    uint8_t tag;
    uint16_t string_index;
} CavaConstantPoolStringInfo;

typedef struct {
    uint8_t tag;
    uint32_t bytes;
} CavaConstantPoolIntegerInfo;

typedef struct {
    uint8_t tag;
    uint32_t bytes;
} CavaConstantPoolFloatInfo;

typedef struct {
    uint8_t tag;
    uint32_t high_bytes;
    uint32_t low_bytes;
} CavaConstantPoolLongInfo;

typedef struct {
    uint8_t tag;
    uint32_t high_bytes;
    uint32_t low_bytes;
} CavaConstantPoolDoubleInfo;

typedef struct {
    uint8_t tag;
    uint16_t name_index;
    uint16_t descriptor_index;
} CavaConstantPoolNameAndTypeInfo;

typedef struct {
    uint8_t tag;
    uint16_t length;
    uint8_t* bytes;
} CavaConstantPoolUtf8Info;

typedef struct {
    uint8_t tag;
    uint8_t reference_kind;
    uint16_t reference_index;
} CavaConstantPoolMethodHandleInfo;

typedef struct {
    uint8_t tag;
    uint16_t descriptor_index;
} CavaConstantPoolMethodTypeInfo;

typedef struct {
    uint8_t tag;
    uint16_t bootstrap_method_attr_index;
    uint16_t name_and_type_index;
} CavaConstantPoolInvokeDynamicInfo;

typedef union {
    CavaConstantPoolClassInfo class_info;
    CavaConstantPoolFieldRefInfo fieldref_info;
    CavaConstantPoolMethodRefInfo methodref_info;
    CavaConstantPoolInterfaceMethodRefInfo interface_methodref_info;
    CavaConstantPoolStringInfo string_info;
    CavaConstantPoolIntegerInfo integer_info;
    CavaConstantPoolFloatInfo float_info;
    CavaConstantPoolLongInfo long_info;
    CavaConstantPoolDoubleInfo double_info;
    CavaConstantPoolNameAndTypeInfo name_and_type_info;
    CavaConstantPoolMethodHandleInfo method_handle_info;
    CavaConstantPoolMethodTypeInfo method_type_info;
    CavaConstantPoolInvokeDynamicInfo invoke_dynamic_info;
    CavaConstantPoolUtf8Info utf8_info;
} CavaConstantPoolInfo;

typedef enum {
    CavaConstantPoolTagClass = 7,
    CavaConstantPoolTagFieldRef = 9,
    CavaConstantPoolTagMethodRef = 10,
    CavaConstantPoolTagInterfaceMethodRef = 11,
    CavaConstantPoolTagString = 8,
    CavaConstantPoolTagInteger = 3,
    CavaConstantPoolTagFloat = 4,
    CavaConstantPoolTagLong = 5,
    CavaConstantPoolTagDouble = 6,
    CavaConstantPoolTagNameAndType = 12,
    CavaConstantPoolTagUtf8 = 1,
    CavaConstantPoolTagMethodHandle = 15,
    CavaConstantPoolTagMethodType = 16,
    CavaConstantPoolTagInvokeDynamic = 18,
} CavaConstantPoolTags;