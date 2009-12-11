.PHONY: all check bsd-games clean distclean

all:
	cd include && $(MAKE) all
	cd backend && $(MAKE) all
	cp backend/lljvm-backend .
	cd java && $(MAKE) all
	cd libc && $(MAKE) all
	cd java && $(MAKE) dist
	cp java/dist/lljvm.jar .

check: all
	cd test && $(MAKE) -s check

bsd-games: all
	cd test/bsd-games && $(MAKE) clean all

clean:
	cd include && $(MAKE) clean
	cd thirdparty && $(MAKE) clean
	cd java && $(MAKE) clean
	cd backend && $(MAKE) clean
	cd libc && $(MAKE) clean
	cd test && $(MAKE) clean
	rm -f lljvm.jar lljvm-backend

distclean:
	cd thirdparty && $(MAKE) distclean
