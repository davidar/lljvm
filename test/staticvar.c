#include <stdio.h>

int main() {
    static void *p = (void*)42;
    printf("p = %d\n", p);
}
