.PHONY: all check bsd-games clean distclean

all:
	cd java && $(MAKE) all
	cp java/dist/lljvm.jar .
	cd backend && $(MAKE) all
	cp backend/lljvm-backend .
	cd libc && $(MAKE) all
	cp libc/libc.class .

check: all
	cd test && $(MAKE) -s check

bsd-games: all
	cd test/bsd-games && $(MAKE) clean all

clean:
	cd thirdparty && $(MAKE) clean
	cd java && $(MAKE) clean
	cd backend && $(MAKE) clean
	cd libc && $(MAKE) clean
	cd test && $(MAKE) clean
	rm -f lljvm.jar lljvm-backend libc.class

distclean:
	cd thirdparty && $(MAKE) distclean
