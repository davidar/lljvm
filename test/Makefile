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

CLEANS := $(patsubst %.test,%.clean,$(TESTS))


.PHONY: check clean

check: $(TESTS)

clean: $(CLEANS)
	@rm -Rf expected
	@rm -Rf actual

%.clean : 
	@rm -Rf ./expected/$*
	@rm -Rf ./actual/$*
	@rm -f ./$*
	@rm -f $*.class
	@rm -f $*.output

%.test: %.clean
	@mkdir -p expected/$*
	@mkdir -p actual/$*

	@echo -n "$*... "
	@rm -f $*
	@cc $*.c -o $* -lm
	@./$* foo bar baz > $*.output

	@mv ./$* ./expected/$*/
	@mv ./$*.output ./expected/$*/
	@cp ./$*.c ./expected/$*/

	@../lljvm-cc $*.c -o $* -lm
	@./$* foo bar baz > $*.output

	@mv ./$* ./actual/$*/
	@mv ./$*.output ./actual/$*/
	@mv ./$*.class ./actual/$*/
	@cp ./$*.c ./actual/$*/
	
	@if diff -q ./expected/$*/$*.output ./actual/$*/$*.output >/dev/null; then echo PASS; else \
	    echo FAIL; \
	    echo EXPECTED; cat ./expected/$*/$*.output; \
	    echo ACTUAL; cat ./actual/$*/$*.output; \
	fi


	

