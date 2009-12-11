#ifndef _UNISTD_H_
#define _UNISTD_H_

#include <sys/unistd.h>

#ifdef HAVE_RENAME
int _rename(const char *oldpath, const char *newpath);
#endif

#endif
