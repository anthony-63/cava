OUT = bin/cava
SRC = src/*.c src/*/*.c

ARGS = test/Test.class

$(OUT): $(SRC)
	if [ ! -d "bin" ]; then mkdir bin; fi
	gcc -o $(OUT) $(SRC)

run: $(OUT)
	./$(OUT) $(ARGS)