all:
	cd ccs && $(MAKE) all
	cd ces && $(MAKE) all
	cd lib && $(MAKE) all
	llvm-ld -link-as-library ccs.o ces.o lib.o -o ../iconv.o

clean:
	cd ccs && $(MAKE) clean
	cd ces && $(MAKE) clean
	cd lib && $(MAKE) clean
	rm -f ../iconv.o
