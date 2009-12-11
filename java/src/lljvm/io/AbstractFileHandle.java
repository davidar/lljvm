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

import java.io.IOException;

import lljvm.runtime.Error;
import lljvm.runtime.Memory;

/**
 * A minimal implementation of the FileHandle interface.
 * 
 * @author  David Roberts
 */
public abstract class AbstractFileHandle implements FileHandle {
    /** Specifies whether this file descriptor supports reading */
    protected final boolean read;
    /** Specifies whether this file descriptor supports writing */
    protected final boolean write;
    /** Specifies whether to enable synchronous I/O */
    protected final boolean synchronous;
    
    /**
     * Construct a new instance with the given read/write capabilities.
     * 
     * @param read   specifies whether this file descriptor supports
     *               reading
     * @param write  specifies whether this file descriptor supports
     *               writing
     */
    protected AbstractFileHandle(boolean read, boolean write,
                                 boolean synchronous) {
        this.read = read;
        this.write = write;
        this.synchronous = synchronous;
    }
    
    /**
     * Reads the next byte of data.
     * 
     * @return              the next byte of data, or -1 on EOF
     * @throws IOException  if an I/O error occurs
     */
    protected int read() throws IOException {
        return -1;
    }
    
    /**
     * Returns true if there is at least one byte available for reading.
     * 
     * @return              true if there is at least one byte available
     *                      for reading
     * @throws IOException  if an I/O error occurs
     */
    protected boolean available() throws IOException {
        return false;
    }
    
    public int read(int buf, int count) {
        if(!read)
            return Error.errno(Error.EINVAL);
        int num_bytes = 0;
        try {
            while(num_bytes < count) {
                int b = read();
                if(b < 0)
                    break;
                Memory.store(buf++, (byte) b);
                num_bytes++;
                if(!available())
                    break;
            }
        } catch(IOException e) {
            return Error.errno(Error.EIO);
        }
        return num_bytes;
    }
    
    /**
     * Writes the given byte.
     * 
     * @param b             the byte to be written
     * @throws IOException  if an I/O error occurs
     */
    protected void write(int b) throws IOException {}
    
    /**
     * Forces any buffered bytes to be written.
     * 
     * @throws IOException  if an I/O error occurs
     */
    protected void flush() throws IOException {}
    
    public int write(int buf, int count) {
        if(!write)
            return Error.errno(Error.EINVAL);
        int num_bytes = 0;
        try {
            while(num_bytes < count) {
                write(Memory.load_i8(buf++));
                num_bytes++;
            }
            if(synchronous)
                flush();
        } catch(IOException e) {
            return Error.errno(Error.EIO);
        }
        return num_bytes;
    }
    
    public int seek(int offset, int whence) {
        return Error.errno(Error.ESPIPE);
    }
}
