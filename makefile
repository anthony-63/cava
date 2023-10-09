OUT = bin/cava
SRC = src/*.c src/*/*.c

ARGS = test/Test.class

JDK_DIR = jdk-21/

$(OUT): $(SRC)
	if [ ! -d "bin" ]; then mkdir bin; fi
	gcc -o $(OUT) $(SRC)

run: $(OUT)
	./$(OUT) $(ARGS)

jdk: $(JDK_DIR)
	javac -sourcepath $(JDK_DIR)

debug:
	if [ ! -d "bin" ]; then mkdir bin; fi
	gcc -o $(OUT)_debug $(SRC) -g
	gdb $(OUT)_debug