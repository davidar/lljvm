#include <stdio.h>
#include <math.h>

int main() {
    float a = 1.3;
    float b = 4.5;
    printf("%f + %f = %f\n", a, b, a+b);
    printf("0./0 = %f, 1./0 = %f, -1./0 = %f\n", 0./0, 1./0, -1./0);
    printf("cos(pi/6) = %f\n", cos(M_PI/6));
}
