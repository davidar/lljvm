TESTS := \
    argv.test \
    array.test \
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

.PHONY: check

check: ${TESTS}

%.test:
	echo -n "$*... "
	cc $*.c -o $* -lm
	./$* foo bar baz > $*.expected
	rm -f $*
	../lljvm-cc $*.c -o $* -lm
	./$* foo bar baz > $*.actual
	rm -f $* $*.class
	diff -q $*.expected $*.actual >/dev/null && echo PASS || echo FAIL
	rm -f $*.expected $*.actual
