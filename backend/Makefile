CXX := c++
SRCS := main.cpp backend.cpp block.cpp branch.cpp const.cpp function.cpp \
        instruction.cpp loadstore.cpp name.cpp printinst.cpp sections.cpp \
        types.cpp
OPTS := $(shell llvm-config --cxxflags --ldflags \
                            --libs bitreader codegen selectiondag)

all:
	$(CXX) ${SRCS} ${OPTS} -o lljvm-backend
