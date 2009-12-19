#include <stdarg.h>
#include <stdio.h>

void vf(int n, va_list ap) {
    int i;
    for(i = 0; i < n; i++) {
        printf("%d\n", va_arg(ap, int));
        printf("%lld\n", va_arg(ap, long long));
    }
}

void f(int n, ...) {
    va_list ap, ap_copy;
    va_start(ap, n);
    va_copy(ap_copy, ap);
    va_end(ap);
    vf(n, ap_copy);
    va_end(ap_copy);
}

int main() {
    f(2, 1, (long long) 2, 3, (long long) 4);
    return 0;
}
