DIRS := \
    bsd-games \
    jpeg \
    lua \
    datalog

.PHONY: all check clean

all:
	for d in ${DIRS}; do (cd $$d && $(MAKE) all); done

check: all
	for d in ${DIRS}; do (cd $$d && $(MAKE) check); done

clean:
	for d in ${DIRS}; do (cd $$d && $(MAKE) clean); done
