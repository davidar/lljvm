#include <stdio.h>
#include <math.h>

double square(double x) {
    return x * x;
}

double call(double (*f)(double), double x) {
    return f(x);
}

int main() {
    double x = 2.0;
    printf("square(%f) = %f\n", x, call(square, x));
    printf("cos(%f) = %f\n", x, call(cos, x));
    return 0;
}
