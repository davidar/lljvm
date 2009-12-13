#include <stdio.h>

char array[] = {1, 0, 255, 254, 0, 42};

int main() {
    int i;
    for(i = 0; i < 6; i++)
        printf("%d\n", array[i]);
    return 0;
}
