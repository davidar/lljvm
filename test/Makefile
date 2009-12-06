TESTS := \
    argv.test \
    cast.test \
    factorial.test \
    file.test \
    fnptr.test \
    fp.test \
    hello.test \
    primes.test \
    staticvar.test \
    struct.test \
    switch.test \
    va.test

.PHONY: check clean

check: clean ${TESTS}

%.bin: %.c
	cc $*.c -lm -o $*.bin

%.class: %.c
	../lljvm-cc $*.c

%.test: %.bin %.class
	echo EXPECTED
	./$*.bin foo bar baz
	echo ACTUAL
	java -classpath ../lljvm.jar:. $* foo bar baz
	echo

clean:
	rm -f *.bc *.j *.class *.bin
