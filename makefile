OUT = bin/cava
SRC = src/*.c src/*/*.c

ARGS = test/Test.class

JDK_DIR = jdk-21/

$(OUT): $(SRC)
	@if [ ! -d "bin" ]; then mkdir bin; fi
	@gcc -o $(OUT) $(SRC) -std=c99

run: $(OUT)
	@./$(OUT) $(ARGS)

dbgnogdb: $(OUT)
	@if [ ! -d "bin" ]; then mkdir bin; fi
	@gcc -o $(OUT)_debug $(SRC) -g -std=c99 -Wall -Wextra -Wpedantic -Werror -Wshadow -Wformat=2 -Wconversion -Wunused-parameter -fsanitize=address,undefined
	@./$(OUT) $(ARGS)

debug:
	@if [ ! -d "bin" ]; then mkdir bin; fi
	@gcc -o $(OUT)_debug $(SRC) -g -std=c99
	gdb $(OUT)_debug