# TODO: refactor duplicate code

DIRS := \
    newlib \
    bsd-games \
    jpeg \
    lua \
    datalog \
    zlib \
    libpng
ARCHIVES := \
    newlib-1.17.0.tar.gz \
    bsd-games-2.17.tar.gz \
    jpeg-7.tar.gz \
    lua-5.1.4.tar.gz \
    datalog-1.4.tar.gz \
    zlib-1.2.3.tar.gz \
    libpng-1.2.41.tar.gz

newlib: newlib-1.17.0.tar.gz
	tar -zxvf $<
	patch -p1 < newlib-1.17.0.patch
	rm -rf newlib
	mv -f newlib-1.17.0 newlib

newlib-1.17.0.tar.gz:
	wget -c ftp://sources.redhat.com/pub/newlib/$@ -O $@.part
	mv -f $@.part $@

bsd-games: bsd-games-2.17.tar.gz
	tar -zxvf $<
	rm -rf bsd-games
	mv -f bsd-games-2.17 bsd-games

bsd-games-2.17.tar.gz:
	wget -c ftp://ibiblio.org/pub/Linux/games/$@ -O $@.part
	mv -f $@.part $@

jpeg: jpeg-7.tar.gz
	tar -zxvf $<
	rm -rf jpeg
	mv -f jpeg-7 jpeg

jpeg-7.tar.gz:
	wget -c http://ijg.org/files/jpegsrc.v7.tar.gz -O $@.part
	mv -f $@.part $@

lua: lua-5.1.4.tar.gz
	tar -zxvf $<
	patch -p1 < lua-5.1.4.patch
	rm -rf lua
	mv -f lua-5.1.4 lua

lua-5.1.4.tar.gz:
	wget -c http://www.lua.org/ftp/$@ -O $@.part
	mv -f $@.part $@

datalog: datalog-1.4.tar.gz
	tar -zxvf $<
	patch -p1 < datalog-1.4.patch
	rm -rf datalog
	mv -f datalog-1.4 datalog

datalog-1.4.tar.gz:
	wget -c http://www.ccs.neu.edu/home/ramsdell/tools/datalog/$@ -O $@.part
	mv -f $@.part $@

zlib: zlib-1.2.3.tar.gz
	tar -zxvf $<
	patch -p1 < zlib-1.2.3.patch
	rm -rf zlib
	mv -f zlib-1.2.3 zlib

zlib-1.2.3.tar.gz:
	wget -c http://zlib.net/fossils/$@ -O $@.part
	mv -f $@.part $@

libpng: libpng-1.2.41.tar.gz
	tar -zxvf $<
	rm -rf libpng
	mv -f libpng-1.2.41 libpng

libpng-1.2.41.tar.gz:
	wget -c http://prdownloads.sourceforge.net/libpng/$@ -O $@.part
	mv -f $@.part $@

clean:
	rm -rf ${DIRS}

distclean: clean
	rm -f ${ARCHIVES}
