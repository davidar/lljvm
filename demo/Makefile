# TODO: extract code from Makefiles in subdirectories to a common Makefile

DIRS := \
    bsd-games \
    jpeg \
    lua \
    datalog \
    zlib \
    libpng

.PHONY: all check clean

all:
	for d in ${DIRS}; do (cd $$d && $(MAKE) all); done

check: all
	for d in ${DIRS}; do (cd $$d && $(MAKE) check); done

clean:
	for d in ${DIRS} jar; do (cd $$d && $(MAKE) clean); done
