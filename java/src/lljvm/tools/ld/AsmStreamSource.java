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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lljvm.util.StreamUtils;

public class AsmStreamSource extends AbstractAsmSource {
    
    private final String name;
    
    private byte[] inputBytes;
    private int inputLen;

    private final InputStream in;
    private final OutputStream out;
    private int outCount;
    
    AsmStreamSource(String name, InputStream in, OutputStream out) {
        super();
        this.name = name;
        this.in = in;
        this.out = out;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected InputStream createInputStream() throws IOException {
        if (inputBytes==null)
            prepareInput();
        return new ByteArrayInputStream(inputBytes, 0, inputLen);
    }

    private void prepareInput() throws IOException {
        inputBytes = new byte[8192];
        inputLen = 0;
        for(;;) {
            if (inputLen>=inputBytes.length) {
                byte[] a = new byte[inputLen<<1];
                System.arraycopy(inputBytes, 0, a, 0, inputBytes.length);
                inputBytes = a;
            }
            int r = in.read(inputBytes,inputLen,inputBytes.length - inputLen);
            if (r<0)
                return;
            inputLen+=r;
        }
    }
    
    
    
    @Override
    protected OutputStream createOutputStream() throws IOException {
        if ((outCount++)>0)
            throw new IllegalStateException("Only a single output stream may be created");
        return StreamUtils.noClose(out);
    }

    @Override
    protected void cleanup() {
        //nothing needed
    }

    @Override
    protected void complete()  {
        //nothing needed
    }


    
    
}
