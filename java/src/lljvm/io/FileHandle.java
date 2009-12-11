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

package lljvm.io;

import java.io.Closeable;

/**
 * Interface for performing operations on a file descriptor.
 * 
 * @author  David Roberts
 */
public interface FileHandle extends Closeable {
    /**
     * Read from this file descriptor.
     * 
     * @param buf    the buffer to read the bytes into
     * @param count  the maximum number of bytes to read
     * @return       the number of bytes read on success, -1 on error
     */
    public int read(int buf, int count);
    
    /**
     * Write to this file descriptor.
     * 
     * @param buf    the buffer of bytes to be written
     * @param count  the maximum number of bytes to write
     * @return       the number of bytes written on success, -1 on error
     */
    public int write(int buf, int count);
    
    /**
     * Reposition file descriptor offset.
     * 
     * @param offset  where to reposition the offset according to the
     *                directive whence
     * @param whence  specifies the reference point to which offset refers
     * @return        the resulting offset on success, -1 on error
     */
    public int seek(int offset, int whence);
}
