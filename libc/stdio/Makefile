OBJS := \
    asiprintf.o \
    asniprintf.o \
    asnprintf.o \
    asprintf.o \
    clearerr.o \
    diprintf.o \
    dprintf.o \
    fcloseall.o \
    fclose.o \
    fdopen.o \
    feof.o \
    ferror.o \
    fflush.o \
    fgetc.o \
    fgetpos.o \
    fgets.o \
    fgetwc.o \
    fgetws.o \
    fileno.o \
    findfp.o \
    fiprintf.o \
    fiscanf.o \
    flags.o \
    fmemopen.o \
    fopen.o \
    fopencookie.o \
    fprintf.o \
    fputc.o \
    fputs.o \
    fputwc.o \
    fputws.o \
    fread.o \
    freopen.o \
    fscanf.o \
    fseek.o \
    fseeko.o \
    fsetpos.o \
    ftell.o \
    ftello.o \
    funopen.o \
    fvwrite.o \
    fwalk.o \
    fwide.o \
    fwrite.o \
    getc.o \
    getchar.o \
    getchar_u.o \
    getc_u.o \
    getdelim.o \
    getline.o \
    gets.o \
    getw.o \
    getwc.o \
    getwchar.o \
    iprintf.o \
    iscanf.o \
    makebuf.o \
    mktemp.o \
    open_memstream.o \
    perror.o \
    printf.o \
    putc.o \
    putchar.o \
    putchar_u.o \
    putc_u.o \
    puts.o \
    putw.o \
    putwc.o \
    putwchar.o \
    refill.o \
    remove.o \
    rename.o \
    rewind.o \
    rget.o \
    scanf.o \
    sccl.o \
    setbuf.o \
    setbuffer.o \
    setlinebuf.o \
    setvbuf.o \
    siprintf.o \
    siscanf.o \
    sniprintf.o \
    snprintf.o \
    sprintf.o \
    sscanf.o \
    stdio.o \
    svfiprintf.o \
    svfiscanf.o \
    svfprintf.o \
    svfscanf.o \
    tmpfile.o \
    tmpnam.o \
    ungetc.o \
    ungetwc.o \
    vasiprintf.o \
    vasniprintf.o \
    vasnprintf.o \
    vasprintf.o \
    vdiprintf.o \
    vdprintf.o \
    vfiprintf.o \
    vfiscanf.o \
    vfprintf.o \
    vfscanf.o \
    viprintf.o \
    viscanf.o \
    vprintf.o \
    vscanf.o \
    vsiprintf.o \
    vsiscanf.o \
    vsniprintf.o \
    vsnprintf.o \
    vsprintf.o \
    vsscanf.o \
    wbuf.o \
    wsetup.o

DIR := stdio
LEVEL := ..
include ${LEVEL}/Makefile.common

CFLAGS := ${CFLAGS} -D_WANT_IO_LONG_LONG

vfprintf.o: ${SOURCE_DIR}/${DIR}/vfprintf.c
	$(CC) ${CFLAGS} -fshort-enums -c $< -o $@

vfiprintf.o: ${SOURCE_DIR}/${DIR}/vfprintf.c
	$(CC) ${CFLAGS} -fshort-enums -DINTEGER_ONLY -c $< -o $@

svfprintf.o: ${SOURCE_DIR}/${DIR}/vfprintf.c
	$(CC) ${CFLAGS} -fshort-enums -DSTRING_ONLY -c $< -o $@

svfiprintf.o: ${SOURCE_DIR}/${DIR}/vfprintf.c
	$(CC) ${CFLAGS} -fshort-enums -DINTEGER_ONLY -DSTRING_ONLY -c $< -o $@

vfscanf.o: ${SOURCE_DIR}/${DIR}/vfscanf.c
	$(CC) ${CFLAGS} -c $< -o $@

vfiscanf.o: ${SOURCE_DIR}/${DIR}/vfscanf.c
	$(CC) ${CFLAGS} -DINTEGER_ONLY -c $< -o $@

svfscanf.o: ${SOURCE_DIR}/${DIR}/vfscanf.c
	$(CC) ${CFLAGS} -DSTRING_ONLY -c $< -o $@

svfiscanf.o: ${SOURCE_DIR}/${DIR}/vfscanf.c
	$(CC) ${CFLAGS} -DINTEGER_ONLY -DSTRING_ONLY -c $< -o $@
