#include <stdio.h>
#include <stdlib.h>

const char *sw(int x) {
    switch(x) {
    case 4: return "four";
    case 5: return "five";
    case 6: return "six";
    default: return "default";
    }
}

int main() {
    int x = 5, y = 7;
    printf("sw(%d) = %s\n", x, sw(x));
    printf("sw(%d) = %s\n", y, sw(y));
    return 0;
}
