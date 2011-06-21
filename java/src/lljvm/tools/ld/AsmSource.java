/*
* Copyright (c) 2011 Joshua Arnold
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
package lljvm.tools.ld;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A single ASM source file to be processed by the linker.
 * @author Joshua Arnold
 */
public interface AsmSource extends Closeable {
    
    /**
     * The logical name of the file, to be used for error reporting and the like.
     * @return the name.
     */
    String getName();
    
    /**
     * Returns an input stream for reading the source.  This may be called multiple times, but the
     * old stream must be closed before a new one can be opened. 
     * @return  The stream
     * @throws IOException if an IO error occurs while opening the stream.
     * @throws IllegalStateException if called while the previous stream is still open.
     */
    InputStream startInput() throws IOException;
    
    /**
     * Returns an output stream for writing the processed source.   This may only be called once, but it
     * may be written to at the same time that the {@linkplain #startInput() input} is read from.
     * @return the output stream.
     * @throws IOException
     */
    OutputStream startOutput() throws IOException;
    
    void close() throws IOException;
}

