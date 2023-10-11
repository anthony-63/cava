#include <stdio.h>
#include "class/class.h"

#define JDK_PATH "jdk-8"

const int debugging = 0;

int main() {
    // CavaClassFile class = read_class_file("test/Test.class");

    // printf("Loading jdk-8...\n");
    // int file_count = 0;
    // load_classpath("jdk-8/");
    int file_count = 0;
    load_classpath(NULL, "test", &file_count);
    CavaClassFile* dest = (CavaClassFile*)malloc(sizeof(CavaClassFile) * (file_count + 2));
    file_count = 0;
    load_classpath(dest, "test", &file_count);
    printf("Loaded %d classfiles\n", file_count);
}