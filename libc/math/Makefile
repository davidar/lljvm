OBJS := \
    sf_nan.o \
    s_isinfd.o \
    s_isnand.o \
    s_ldexp.o \
    s_mathcnst.o \
    s_numtest.o

DIR := math
LEVEL := ..
include ${LEVEL}/Makefile.common

CFLAGS := ${CFLAGS} -I${SOURCE_DIR}/../libm/common \
                    -I${SOURCE_DIR}/../libm/mathfp

sf_nan.o: ${SOURCE_DIR}/../libm/common/sf_nan.c
	$(CC) ${CFLAGS} -c $< -o $@

s_isinfd.o: ${SOURCE_DIR}/../libm/common/s_isinfd.c
	$(CC) ${CFLAGS} -c $< -o $@

s_isnand.o: ${SOURCE_DIR}/../libm/common/s_isnand.c
	$(CC) ${CFLAGS} -c $< -o $@

s_ldexp.o: ${SOURCE_DIR}/../libm/mathfp/s_ldexp.c
	$(CC) ${CFLAGS} -c $< -o $@

s_mathcnst.o: ${SOURCE_DIR}/../libm/mathfp/s_mathcnst.c
	$(CC) ${CFLAGS} -c $< -o $@

s_numtest.o: ${SOURCE_DIR}/../libm/mathfp/s_numtest.c
	$(CC) ${CFLAGS} -c $< -o $@
