#include <stdio.h>

typedef struct {
    const char *a, *b, *c, *d;
} struct_t;

struct_t array[] = {
    {"a1", "b1", "c1", "d1"},
    {"a2", "b2", "c2"},
    {"a3", "b3"},
    {"a4"},
    {NULL}
};

int main() {
    struct_t *p;
    for(p = array; p->a; p++)
        printf("{%s, %s, %s, %s}\n", p->a, p->b, p->c, p->d);
    return 0;
}
