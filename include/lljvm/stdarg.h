#ifndef __STDARG_H
#define __STDARG_H

#include <stddef.h>

void *unpack(void *addrp, size_t size);

typedef __builtin_va_list va_list;
#ifdef __GNUC__
#define __GNUC_VA_LIST 1
typedef __builtin_va_list __gnuc_va_list;
#endif

#define va_start(ap, param) __builtin_va_start(ap, param)
#define va_end(ap)          __builtin_va_end(ap)
#define va_arg(ap, type)    *(type*)unpack(&ap, sizeof(type))

#if __STDC_VERSION__ >= 199900L || !defined(__STRICT_ANSI__)
#define va_copy(dest, src) __builtin_va_copy(dest, src)
#endif
#define __va_copy(dest, src) __builtin_va_copy(dest, src)

#endif
