#include <stdio.h>

typedef struct {
    unsigned char a;
    long long b;
} struct_t;
struct_t s = {200, 250};

int main() {
    printf("s = {%d, %lld}\n", s.a, ++s.b);
    return 0;
}
