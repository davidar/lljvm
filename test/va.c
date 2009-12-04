#include <stdarg.h>
#include <stdio.h>

int vsum(int n, va_list ap) {
    int i, sum = 0;
    for(i = 0; i < n; i++)
        sum += va_arg(ap, int);
    return sum;
}

int sum(int n, ...) {
    va_list ap, ap_copy;
    va_start(ap, n);
    va_copy(ap_copy, ap);
    va_end(ap);
    int sum = vsum(n, ap_copy);
    va_end(ap_copy);
    return sum;
}

int main() {
    printf("sum(1..5) = %d\n", sum(5, 1, 2, 3, 4, 5));
    return 0;
}
