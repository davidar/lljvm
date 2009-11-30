#include <stdio.h>

typedef struct {
    unsigned char a;
    int b;
} struct_t;
struct_t s = {200,250};

int main() {
    printf("s = {%d, %d}\n", s.a, ++s.b);
}
