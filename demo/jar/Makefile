all: lljvm-demo.jar

lljvm-demo.jar: demo Main.class
	echo "Main-Class: Main" > MANIFEST.MF
	echo "Class-Path: lljvm-${VERSION}.jar" >> MANIFEST.MF
	cd .. && jar -cvfm jar/$@ jar/MANIFEST.MF -C jar Main.class \
	    -C bsd-games banner.class -C bsd-games morse.class \
	    -C bsd-games number.class -C bsd-games pig.class \
	    -C jpeg cjpeg.class -C jpeg djpeg.class -C jpeg jpegtran.class \
	    -C jpeg rdjpgcom.class -C jpeg wrjpgcom.class \
	    -C lua liblua.class -C lua lua.class -C lua luac.class \
	    -C datalog datalog.class \
	    -C zlib libz.class -C zlib minigzip.class

demo:
	cd .. && $(MAKE) all

%.class: %.java
	javac $<

clean:
	rm -f MANIFEST.MF Main.class lljvm-demo.jar
