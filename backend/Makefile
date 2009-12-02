CXX := c++
OBJS := main.o backend.o block.o branch.o const.o function.o instruction.o \
        loadstore.o name.o printinst.o sections.o types.o
CFLAGS := $(shell llvm-config --cxxflags --ldflags \
                              --libs bitreader codegen selectiondag)

.PHONY: all clean

all: lljvm-backend

lljvm-backend: ${OBJS}
	$(CXX) ${OBJS} ${CFLAGS} -o lljvm-backend

clean:
	rm -f lljvm-backend

.cpp.o:
	$(CXX) ${CFLAGS} -c $<

${OBJS}: backend.h
