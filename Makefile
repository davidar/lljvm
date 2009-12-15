.PHONY: all doc check clean distclean

all:
	cd include && $(MAKE) all
	cd backend && $(MAKE) all
	cp backend/lljvm-backend .
	cd java && $(MAKE) all
	cd libc && $(MAKE) all
	cd java && $(MAKE) dist
	cp java/dist/lljvm.jar .

doc:
	cd java && $(MAKE) doc

check: all
	cd test && $(MAKE) -s check

clean:
	cd include && $(MAKE) clean
	cd thirdparty && $(MAKE) clean
	cd java && $(MAKE) clean
	cd backend && $(MAKE) clean
	cd libc && $(MAKE) clean
	cd demo && $(MAKE) clean
	rm -f lljvm.jar lljvm-backend

distclean: clean
	cd thirdparty && $(MAKE) distclean
