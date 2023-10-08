#include "reader.h"

CavaClassReader* class_reader_init(const char* file_name) {
    CavaClassReader* reader = (CavaClassReader*)malloc(sizeof(reader));
    reader->file_name = (char*)malloc(strlen(file_name));
    strcpy(reader->file_name, file_name);

    reader->index = 0;

    FILE* fp = fopen(file_name, "rb");
    if(fp == NULL) {
        fprintf(stderr, "Failed to open file: %s\n", file_name);
        exit(-1);
    }

    fseek(fp, 0, SEEK_END);
    reader->bytes_length = ftell(fp);
    rewind(fp);

    reader->bytes = (uint8_t*)malloc(reader->bytes_length * sizeof(uint8_t));
    fread(reader->bytes, reader->bytes_length, 1, fp);
    fclose(fp);

    return reader;
}

uint8_t class_reader_get_u8(CavaClassReader* reader) {
    if(reader->index >= reader->bytes_length) {
        fprintf(stderr, "Tried to read past file bounds with index of %d and a file size of %d\n", reader->index, reader->bytes_length);
        exit(-1);
    }

    return reader->bytes[reader->index++];
}

uint16_t class_reader_get_u16(CavaClassReader* reader) {
    uint16_t res = 0;
    uint8_t bytes[2];
    
    if(reader->index + 1 >= reader->bytes_length) {
        fprintf(stderr, "Tried to read past file bounds with index of %d and a file size of %d\n", reader->index + 1, reader->bytes_length);
        exit(-1);
    }

    bytes[1] = reader->bytes[reader->index++];
    bytes[0] = reader->bytes[reader->index++];
    memcpy(&res, &bytes, sizeof(res));
    return res;
}

uint32_t class_reader_get_u32(CavaClassReader* reader) {
    uint32_t res = 0;
    uint8_t bytes[4];
    
    if(reader->index + 3 >= reader->bytes_length) {
        fprintf(stderr, "Tried to read past file bounds with index of %d and a file size of %d\n", reader->index + 3, reader->bytes_length);
        exit(-1);
    }

    bytes[3] = reader->bytes[reader->index++];
    bytes[2] = reader->bytes[reader->index++];
    bytes[1] = reader->bytes[reader->index++];
    bytes[0] = reader->bytes[reader->index++];

    memcpy(&res, &bytes, sizeof(res));
    return res;
}