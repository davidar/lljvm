#include <stdio.h>

int main() {
    char b;
    unsigned char ub;
    short s;
    unsigned short us;
    int i;
    unsigned int ui;
    long long l;
    unsigned long long ul;
    
    b = 0x7f;
    printf("%d\n", ++b);
    ub = 0xff;
    printf("%d\n", ++b);
    l = 800;
    printf("%d\n", (char)l);
    return 0;
}
