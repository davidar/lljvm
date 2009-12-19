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
	if diff -q $*.expected $*.actual >/dev/null; then echo PASS; else \
	    echo FAIL; \
	    echo EXPECTED; cat $*.expected; \
	    echo ACTUAL; cat $*.actual; \
	fi
	rm -f $*.expected $*.actual
