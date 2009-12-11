all: newlib

newlib: ../thirdparty/newlib
	rm -rf $@
	cp -Rv $</newlib/libc/include $@

../thirdparty/newlib:
	cd ../thirdparty && $(MAKE) newlib

clean:
	rm -rf newlib
