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

package lljvm.runtime;

/**
 * Provides methods and constants related to I/O.
 * 
 * @author  David Roberts
 */
public final class IO {
    public static final int O_RDONLY   = 0x0000;
    public static final int O_WRONLY   = 0x0001;
    public static final int O_RDWR     = 0x0002;
    public static final int O_APPEND   = 0x0008;
    public static final int O_CREAT    = 0x0200;
    public static final int O_TRUNC    = 0x0400;
    public static final int O_EXCL     = 0x0800;
    public static final int O_SYNC     = 0x2000;
    public static final int O_NONBLOCK = 0x4000;
    public static final int O_NOCTTY   = 0x8000;
    
    /**
     * Prevent this class from being instantiated.
     */
    private IO() {}
}
