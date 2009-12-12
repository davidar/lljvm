#include <stdio.h>
#include <setjmp.h>

static jmp_buf buf1, buf2;
 
void second(void) {
    printf("second\n");
    longjmp(buf1, 0);
}
 
void first(void) {
    setjmp(buf2);
    second();
    printf("first\n");
}
 
int main() {   
    if(!setjmp(buf1))
        first();
    else
        printf("main\n");
    return 0;
}
