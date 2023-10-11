#include "class.h"
#include "reader.h"

#include <assert.h>
#include "../xplatform/dirent.h"

extern int debugging;

int is_class_file(char* s) {
    if(s != NULL) {
        size_t sz = strlen(s);
        if(sz >= 6 &&
            s[sz-6] == '.' && 
            s[sz-5] == 'c' && 
            s[sz-4] == 'l' &&
            s[sz-3] == 'a' &&
            s[sz-2] == 's' && 
            s[sz-1] == 's') {
            return 1;
        }
    }
    return 0;
}

void get_classfile_dependencies(char** dest, CavaClassFile file) {
    char* this_class = "";
    for(int i = 0; i < file.constant_pool_count; i++) {
        if(file.constant_pool[i].class_info.tag == CavaConstantPoolTagClass && i == file.this_class - 1) {
            this_class = resolve_utf8(file, file.constant_pool[i].class_info.name_index);
        } else if(file.constant_pool[i].class_info.tag == CavaConstantPoolTagClass) {
            printf("Depends on: %s\n", resolve_utf8(file, file.constant_pool[i].class_info.name_index));
        }
    }
}

char* resolve_utf8(CavaClassFile file, int index) {
    if(index >= file.constant_pool_count - 1){
        fprintf(stderr, "Invalid index in constant pool: %d\n", index);
        exit(-1);
    }
    if(file.constant_pool[index - 1].utf8_info.tag != CavaConstantPoolTagUtf8) {
        fprintf(stderr, "Invalid UTF8 index in constant pool expected type %s at index %d but got type %s\n", CavaConstantPoolTagStrings[CavaConstantPoolTagUtf8 - 1], index, CavaConstantPoolTagStrings[file.constant_pool[index].utf8_info.tag - 1]);
        exit(-1);
    }
    return (char*)file.constant_pool[index-1].utf8_info.bytes;
}

void load_classpath(CavaClassFile* dest, char* classpath, int* count) {
    DIR *dir;
    struct dirent *entry;

    if (!(dir = opendir(classpath))){
        printf("INVALID_DIR: %s", classpath);
        exit(1);
    }

    while ((entry = readdir(dir)) != NULL) {
        if (entry->d_type == DT_DIR) {
            char path[1024];
            if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0)
                continue;
            sprintf(path, "%s/%s", classpath, entry->d_name);
            load_classpath(dest, path, count);
        } else {
            if(!is_class_file(entry->d_name)) continue;
            (*count)++;
            char path[1024];
            // printf("ALLOC F: %08x | '%s'\n", path, entry->d_name);
            sprintf(path, "%s/%s", classpath, entry->d_name);
            // printf("%s: %s\n", classpath, entry->d_name);
            if(dest == NULL) continue;
            printf("Loading: %s\n", path);
            dest[(*count)-1] = read_class_file(path);
            printf("Loaded: %s\n", path);
            printf("Getting dependencies for: %s\n", path);
            get_classfile_dependencies(NULL, dest[(*count)-1]);
        }
    }
    closedir(dir);
}


CavaClassFile read_class_file(const char* file_name) {
    CavaClassReader* reader = class_reader_init(file_name);
    CavaClassFile class;

    class.magic = class_reader_get_u32(reader);
    class.minor_version = class_reader_get_u16(reader);
    class.major_version = class_reader_get_u16(reader);
    
    if(class.magic != 0xcafebabe) {
        fprintf(stderr, "Invalid ClassFile magic: %08x\n", class.magic);
        exit(-1);
    }

    if(debugging) printf("---------- CLASS HEADER ----------\n");
    if(debugging) printf("MAGIC: %08x\n", class.magic);
    if(debugging) printf("VERSION: %d.%d\n", class.major_version, class.minor_version);
    
    read_constant_pool(&class, reader);
    
    if(debugging) printf("---------- CLASS METADATA ----------\n");
    class.access_flags = class_reader_get_u16(reader);
    class.this_class = class_reader_get_u16_little(reader);
    class.super_class = class_reader_get_u16_little(reader);

    if(debugging) printf("ACCESS_FLAGS: %04x\n", class.access_flags);
    if(debugging) printf("THIS_CLASS: %d\n", class.this_class);
    if(debugging) printf("SUPER_CLASS: %d\n", class.super_class);

    assert(class.constant_pool[class.this_class - 1].class_info.tag == CavaConstantPoolTagClass);
    assert(class.constant_pool[class.super_class - 1].class_info.tag == CavaConstantPoolTagClass);

    class.interfaces_count = class_reader_get_u16(reader);
    if(class.interfaces_count > 0) {
        fprintf(stderr, "Cava does not support interfaces yet!\n");
        // exit(-1);
    }

    return class;
}

void read_constant_pool(CavaClassFile* class, CavaClassReader* reader) {
    if(debugging) printf("---------- CONSTANT POOL ----------\n");
    class->constant_pool_count = class_reader_get_u16(reader);
    if(debugging) printf("COUNT: %d\n" , class->constant_pool_count);

    class->constant_pool = (CavaConstantPoolInfo*)malloc(sizeof(CavaConstantPoolInfo) * class->constant_pool_count);

    for(int i = 0; i < class->constant_pool_count; i++) {
        uint8_t tag = class_reader_get_u8(reader);
        if(tag == 0) break;
        if(debugging) printf("[ENTRY %d] ", i + 1);
        if(debugging) printf("TAG: %s, ", CavaConstantPoolTagStrings[tag - 1]);

        switch(tag) {
        case CavaConstantPoolTagClass:
            class->constant_pool[i].class_info.tag = tag;
            class->constant_pool[i].class_info.name_index = class_reader_get_u16(reader);
            if(debugging) printf("NAME_INDEX: %d", class->constant_pool[i].class_info.name_index);
            break;
        case CavaConstantPoolTagFieldRef:
            class->constant_pool[i].fieldref_info.tag = tag;
            class->constant_pool[i].fieldref_info.class_index = class_reader_get_u16(reader);
            class->constant_pool[i].fieldref_info.name_and_type_index = class_reader_get_u16(reader);
            if(debugging) printf("CLASS_INDEX: %d, NAME_AND_TYPE_INDEX: %d", class->constant_pool[i].fieldref_info.class_index, class->constant_pool[i].fieldref_info.name_and_type_index);
            break;
        case CavaConstantPoolTagMethodRef:
            class->constant_pool[i].methodref_info.tag = tag;
            class->constant_pool[i].methodref_info.class_index = class_reader_get_u16(reader);
            class->constant_pool[i].methodref_info.name_and_type_index = class_reader_get_u16(reader);
            if(debugging) printf("CLASS_INDEX: %d, NAME_AND_TYPE_INDEX: %d", class->constant_pool[i].methodref_info.class_index, class->constant_pool[i].methodref_info.name_and_type_index);
            break;
        case CavaConstantPoolTagInterfaceMethodRef:
            class->constant_pool[i].interface_methodref_info.tag = tag;
            class->constant_pool[i].interface_methodref_info.class_index = class_reader_get_u16(reader);
            class->constant_pool[i].interface_methodref_info.name_and_type_index = class_reader_get_u16(reader);
            if(debugging) printf("CLASS_INDEX: %d, NAME_AND_TYPE_INDEX: %d", class->constant_pool[i].interface_methodref_info.class_index, class->constant_pool[i].interface_methodref_info.name_and_type_index);
            break;
        case CavaConstantPoolTagString:
            class->constant_pool[i].string_info.tag = tag;
            class->constant_pool[i].string_info.string_index = class_reader_get_u16(reader);
            if(debugging) printf("STRING_INDEX: %d", class->constant_pool[i].string_info.string_index);
            break;
        case CavaConstantPoolTagInteger:
            class->constant_pool[i].integer_info.tag = tag;
            class->constant_pool[i].integer_info.bytes = class_reader_get_u32(reader);
            if(debugging) printf("STRING_INDEX: %d", class->constant_pool[i].string_info.string_index);
            break;
        case CavaConstantPoolTagFloat:
            class->constant_pool[i].float_info.tag = tag;
            class->constant_pool[i].float_info.bytes = class_reader_get_u32(reader);
            if(debugging) printf("STRING_INDEX: %d", class->constant_pool[i].string_info.string_index);
            break;
        case CavaConstantPoolTagLong:
            class->constant_pool[i].long_info.tag = tag;
            class->constant_pool[i].long_info.high_bytes = class_reader_get_u32(reader);
            class->constant_pool[i].long_info.low_bytes = class_reader_get_u32(reader);
            if(debugging) printf("STRING_INDEX: %d", class->constant_pool[i].string_info.string_index);
            break;
        case CavaConstantPoolTagDouble:
            class->constant_pool[i].double_info.tag = tag;
            class->constant_pool[i].double_info.high_bytes = class_reader_get_u32(reader);
            class->constant_pool[i].double_info.low_bytes = class_reader_get_u32(reader);
            if(debugging) printf("STRING_INDEX: %d", class->constant_pool[i].string_info.string_index);
            break;
        case CavaConstantPoolTagNameAndType:
            class->constant_pool[i].name_and_type_info.tag = tag;
            class->constant_pool[i].name_and_type_info.name_index = class_reader_get_u16(reader);
            class->constant_pool[i].name_and_type_info.descriptor_index = class_reader_get_u16(reader);
            if(debugging) printf("NAME_INDEX: %d, DESCRIPTOR_INDEX: %d", class->constant_pool[i].name_and_type_info.name_index, class->constant_pool[i].name_and_type_info.descriptor_index);
            break;
        case CavaConstantPoolTagUtf8:
            class->constant_pool[i].utf8_info.tag = tag;
            class->constant_pool[i].utf8_info.length = class_reader_get_u16(reader);
            class->constant_pool[i].utf8_info.bytes = (uint8_t*)malloc(sizeof(uint8_t) * class->constant_pool[i].utf8_info.length + 1);
            for(int j = 0; j < class->constant_pool[i].utf8_info.length; j++) {
                class->constant_pool[i].utf8_info.bytes[j] = class_reader_get_u8(reader);
            }
            class->constant_pool[i].utf8_info.bytes[class->constant_pool[i].utf8_info.length] = '\0';
            if(debugging) printf("LEN: %d, BYTES: %s", class->constant_pool[i].utf8_info.length, class->constant_pool[i].utf8_info.bytes);
            break;
        case CavaConstantPoolTagMethodHandle:
            class->constant_pool[i].method_handle_info.tag = tag;
            class->constant_pool[i].method_handle_info.reference_kind = class_reader_get_u8(reader);
            class->constant_pool[i].method_handle_info.reference_index = class_reader_get_u16(reader);
            if(debugging) printf("REF_KIND: %d, REF_INDEX: %d", class->constant_pool[i].method_handle_info.reference_kind, class->constant_pool[i].method_handle_info.reference_index);
            break;
        case CavaConstantPoolTagInvokeDynamic:
            class->constant_pool[i].invoke_dynamic_info.tag = tag;
            class->constant_pool[i].invoke_dynamic_info.bootstrap_method_attr_index = class_reader_get_u16(reader);
            class->constant_pool[i].invoke_dynamic_info.name_and_type_index = class_reader_get_u16(reader);
            if(debugging) printf("BOOTSTRAP_METHOD_ADDR_INDEX: %d, NAME_AND_TYPE_INDEX: %d", class->constant_pool[i].invoke_dynamic_info.bootstrap_method_attr_index, class->constant_pool[i].invoke_dynamic_info.name_and_type_index);
            break;
        default:
            printf("INVALID TAG: %d\n", tag);
            exit(-1);
        }
       if(debugging) printf("\n");
    }
}