#include <stdio.h>

int factorial(int n) {
    return n ? n * factorial(n - 1) : 1;
}

int main() {
    int n = 10;
    printf("%d! = %d\n", n, factorial(n));
}
