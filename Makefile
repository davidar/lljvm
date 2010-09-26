VERSION := 0.3dev

.PHONY: all doc doc-pdf doc-zip check demo clean distclean

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
	rm -rf doc
	mkdir -p doc
	mv java/doc doc/java
	mv backend/doc/html doc/backend

doc-pdf: doc
	cd backend/doc/latex && $(MAKE) all
	mv backend/doc/latex/refman.pdf doc/backend.pdf

doc-zip: doc doc-pdf
	mv doc lljvm-doc-${VERSION}
	zip -r lljvm-doc-${VERSION}.zip lljvm-doc-${VERSION}
	mv lljvm-doc-${VERSION} doc

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
	rm -rf doc
	rm -f lljvm-${VERSION}.jar lljvm-demo-${VERSION}.jar \
	      lljvm-doc-${VERSION}.zip lljvm-backend

distclean: clean
	cd thirdparty && $(MAKE) distclean
