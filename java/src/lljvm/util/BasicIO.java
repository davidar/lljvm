/*
* Copyright (c) 2009 David Roberts <d@vidr.cc>
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package lljvm.util;

import lljvm.runtime.Memory;

/**
 * Provides methods for performing basic I/O.
 * 
 * @author  David Roberts
 */
public final class BasicIO {
    /**
     * Prevent this class from being instantiated.
     */
    private BasicIO() {}
    
    /**
     * Write the given string and a trailing newline to stdout.
     * 
     * @param s  a pointer to the string
     * @return   a non-negative number on success
     */
    public static int puts(int s) {
        System.out.println(Memory.load_string(s));
        return 0;
    }
    
    /**
     * Write the given character to stdout.
     * 
     * @param c  the character to write
     * @return   the character written
     */
    public static int putchar(int c) {
        c &= 0xff;
        System.out.write(c);
        return c;
    }
    
    /**
     * Write the given formatted string to stdout.
     * 
     * @param format  the format string
     * @param args    a pointer to the packed list of arguments
     * @return        a non-negative number on success
     */
    public static int printf(int format, int args) {
        String fmt = Memory.load_string(format);
        for(int i = 0; i < fmt.length(); i++) {
            if(fmt.charAt(i) == '%')
                switch(fmt.charAt(++i)) {
                case 'd':
                case 'i':
                    System.out.print(Memory.load_i32(args)); args += 4; break;
                case 'f':
                    System.out.print((float)Memory.load_f64(args)); args += 8;
                    break;
                case 'c':
                    putchar(Memory.load_i32(args)); args += 4; break;
                case 's':
                    System.out.print(Memory.load_string(
                            Memory.load_i32(args))); args += 4; break;
                case 'l':
                    switch(fmt.charAt(++i)) {
                    case 'l':
                        switch(fmt.charAt(++i)) {
                        case 'd':
                        case 'i':
                            System.out.print(Memory.load_i64(args)); args += 8;
                            break;
                        }
                        break;
                    }
                    break;
                case '%':
                    System.out.print('%'); break;
                }
            else System.out.print(fmt.charAt(i));
        }
        return 0;
    }
}
