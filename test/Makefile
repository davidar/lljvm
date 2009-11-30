check: clean argv.class factorial.class fp.class primes.class staticvar.class \
       struct.class switch.class va.class

%.class: %.c
	../lljvm-cc $*.c
	java -classpath ../lljvm.jar:. $* foo bar baz
	echo

clean:
	rm -f *.bc *.j *.class
