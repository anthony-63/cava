#include "class.h"
#include "reader.h"

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

    printf("---------- CLASS INFO ----------\n");
    printf("MAGIC: %08x\n", class.magic);
    printf("VERSION: %d.%d\n", class.major_version, class.minor_version);
    return class;
}