all: newlib bsd-games

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

clean:
	rm -rf newlib bsd-games

distclean:
	rm -f newlib-1.17.0.tar.gz bsd-games-2.17.tar.gz
