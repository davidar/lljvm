all: newlib

newlib: newlib-1.17.0.tar.gz
	tar -zxvf $<
	patch -p1 < newlib-1.17.0.patch
	mv -f newlib-1.17.0 newlib

newlib-1.17.0.tar.gz:
	wget -c ftp://sources.redhat.com/pub/newlib/$@ -O $@.part
	mv -f $@.part $@

clean:
	rm -rf newlib/

distclean:
	rm -f newlib-1.17.0.tar.gz
