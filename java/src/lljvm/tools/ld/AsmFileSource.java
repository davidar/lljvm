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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lljvm.util.StreamUtils;

public class AsmFileSource extends AbstractAsmSource {
    private final String name;
    
    private final File input;
    private final File output;
    
    private volatile File tmpOut;
    
    
    /**
     * Constructs a source that reads and writes to the same file.
     * @param file the file
     */
    AsmFileSource(File file) {
        this(file,null);
    }
    
    /**
     * Constructs a source that reads from one file and write to another.
     * @param input the input file
     * @param output the output file
     */
    AsmFileSource(File input, File output) {
        super();
        this.input = input;
        this.output = output;
        this.name = input.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected InputStream createInputStream() throws IOException {
        return new FileInputStream(input);
    }

    @Override
    protected OutputStream createOutputStream() throws IOException {
        File writeTo;
        if (output!=null) {
            writeTo = output;
        } else {
            this.tmpOut = writeTo = File.createTempFile("lljvm-ld-"+name, ".asm.tmp");
        }
        writeTo.getAbsoluteFile().getParentFile().mkdirs();
        return new FileOutputStream(writeTo,false);
        
    }

    @Override
    protected void cleanup() {
    }

    @Override
    protected void complete() throws IOException {
        if (output!=null)
            return;
        File tmpOut = this.tmpOut;
        if (tmpOut==null)
            return;
        FileInputStream fis = new FileInputStream(tmpOut);
        try {
            FileOutputStream fos = new FileOutputStream(input);
            try {
                StreamUtils.readFully(fis, fos);
            } finally {
                fos.close();
            }
        } finally {
            fis.close();
        }
    }

}
