OBJS := \
    a64l.o \
    abort.o \
    abs.o \
    __adjust.o \
    assert.o \
    __atexit.o \
    atexit.o \
    atof.o \
    atoff.o \
    atoi.o \
    atol.o \
    atoll.o \
    btowc.o \
    __call_atexit.o \
    calloc.o \
    callocr.o \
    cfreer.o \
    cxa_atexit.o \
    cxa_finalize.o \
    div.o \
    drand48.o \
    dtoa.o \
    dtoastub.o \
    ecvtbuf.o \
    efgcvt.o \
    environ.o \
    envlock.o \
    eprintf.o \
    erand48.o \
    exit.o \
    _Exit.o \
    __exp10.o \
    freer.o \
    gdtoa-gethex.o \
    gdtoa-hexnan.o \
    getenv.o \
    getenv_r.o \
    getopt.o \
    getsubopt.o \
    jrand48.o \
    l64a.o \
    labs.o \
    lcong48.o \
    ldiv.o \
    llabs.o \
    lldiv.o \
    lrand48.o \
    malign.o \
    malignr.o \
    mallinfor.o \
    malloc.o \
    mallocr.o \
    malloptr.o \
    mallstatsr.o \
    mblen.o \
    mblen_r.o \
    mbrlen.o \
    mbrtowc.o \
    mbsinit.o \
    mbsrtowcs.o \
    mbstowcs.o \
    mbstowcs_r.o \
    mbtowc.o \
    mbtowc_r.o \
    mlock.o \
    mprec.o \
    mrand48.o \
    msize.o \
    msizer.o \
    mstats.o \
    mtrim.o \
    nrand48.o \
    on_exit.o \
    putenv.o \
    putenv_r.o \
    pvallocr.o \
    rand48.o \
    rand.o \
    rand_r.o \
    realloc.o \
    reallocf.o \
    reallocr.o \
    seed48.o \
    setenv.o \
    setenv_r.o \
    srand48.o \
    strtod.o \
    strtol.o \
    strtoll.o \
    strtoll_r.o \
    strtoul.o \
    strtoull.o \
    strtoull_r.o \
    system.o \
    __ten_mu.o \
    valloc.o \
    vallocr.o \
    wcrtomb.o \
    wcsrtombs.o \
    wcstol.o \
    wcstoll.o \
    wcstoll_r.o \
    wcstombs.o \
    wcstombs_r.o \
    wcstoul.o \
    wcstoull.o \
    wcstoull_r.o \
    wctob.o \
    wctomb.o \
    wctomb_r.o

DIR := stdlib
LEVEL := ..
include ${LEVEL}/Makefile.common

CFLAGS := ${CFLAGS} -DHAVE_MMAP=0 -DINTERNAL_NEWLIB

mallocr.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_MALLOC -c $< -o $@

freer.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_FREE -c $< -o $@

reallocr.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_REALLOC -c $< -o $@

callocr.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_CALLOC -c $< -o $@

cfreer.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_CFREE -c $< -o $@

malignr.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_MEMALIGN -c $< -o $@

vallocr.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_VALLOC -c $< -o $@

pvallocr.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_PVALLOC -c $< -o $@

mallinfor.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_MALLINFO -c $< -o $@

mallstatsr.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_MALLOC_STATS -c $< -o $@

msizer.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_MALLOC_USABLE_SIZE -c $< -o $@

malloptr.o: ${SOURCE_DIR}/${DIR}/mallocr.c
	$(CC) ${CFLAGS} -DDEFINE_MALLOPT -c $< -o $@
