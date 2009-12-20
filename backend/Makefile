CXX := c++
CFLAGS := $(shell llvm-config --cxxflags --ldflags \
                              --libs bitreader codegen selectiondag)
OBJS := \
    main.o \
    backend.o \
    block.o \
    branch.o \
    const.o \
    function.o \
    instruction.o \
    loadstore.o \
    name.o \
    printinst.o \
    sections.o \
    types.o

.PHONY: all doc clean

all: lljvm-backend

lljvm-backend: ${OBJS}
	$(CXX) ${OBJS} ${CFLAGS} -o lljvm-backend

doc:
	doxygen

clean:
	rm -f *.o lljvm-backend

.cpp.o:
	$(CXX) ${CFLAGS} -c $<

${OBJS}: backend.h
