#include <stdio.h>
#define BUF_SIZE 8
int main() {
    FILE *fin = fopen(__FILE__, "r");
    char buf[BUF_SIZE];
    while(!feof(fin))
        fwrite(buf, sizeof(char),
            fread(buf, sizeof(char), BUF_SIZE, fin),
            stdout);
    fclose(fin);
    return 0;
}
