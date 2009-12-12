TESTS := \
    argv.test \
    cast.test \
    factorial.test \
    file.test \
    fnptr.test \
    fp.test \
    hello.test \
    primes.test \
    setjmp.test \
    staticvar.test \
    struct.test \
    struct2.test \
    switch.test \
    unreachable.test \
    va.test

.PHONY: check clean

check: ${TESTS}

%.bin: %.c
	cc $*.c -lm -o $*.bin

%.class: %.c
	../lljvm-cc $*.c -o $*

%.test: %.bin %.class
	echo EXPECTED
	./$*.bin foo bar baz
	echo ACTUAL
	./$* foo bar baz
	rm -f $*
	echo
