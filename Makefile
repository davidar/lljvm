VERSION := 0.1dev

.PHONY: all doc check demo clean distclean

all:
	cd include && $(MAKE) all
	cd backend && $(MAKE) all
	cp backend/lljvm-backend .
	cd java && $(MAKE) all
	cd libc && $(MAKE) all
	cd java && $(MAKE) dist
	cp java/dist/lljvm.jar lljvm-${VERSION}.jar

doc:
	cd java && $(MAKE) doc
	cd backend && $(MAKE) doc

check: all
	cd test && $(MAKE) -s check

demo: all
	cd demo/jar && $(MAKE) VERSION=${VERSION} all
	cp demo/jar/lljvm-demo.jar lljvm-demo-${VERSION}.jar

clean:
	cd include && $(MAKE) clean
	cd thirdparty && $(MAKE) clean
	cd java && $(MAKE) clean
	cd backend && $(MAKE) clean
	cd libc && $(MAKE) clean
	cd demo && $(MAKE) clean
	rm -f lljvm-${VERSION}.jar lljvm-demo-${VERSION}.jar lljvm-backend

distclean: clean
	cd thirdparty && $(MAKE) distclean
