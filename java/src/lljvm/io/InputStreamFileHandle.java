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
import java.io.InputStream;

/**
 * Implements the FileHandle interface, backed by an InputStream.
 * 
 * @author  David Roberts
 */
public class InputStreamFileHandle extends AbstractFileHandle {
    /** The input stream */
    private InputStream inputStream;
    
    /**
     * Construct a new instance with the given input stream.
     * 
     * @param inputStream  the input stream
     */
    public InputStreamFileHandle(InputStream inputStream) {
        super(true, false, false);
        this.inputStream = inputStream;
    }
    
    protected int read() throws IOException {
        return inputStream.read();
    }
    
    protected boolean available() throws IOException {
        return inputStream.available() != 0;
    }
    
    public void close() throws IOException {
        inputStream.close();
    }
}
