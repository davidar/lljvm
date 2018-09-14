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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import lljvm.runtime.Context;
import lljvm.runtime.Error;
import lljvm.runtime.IO;
import lljvm.runtime.Memory;

/**
 * Implements the FileHandle interface, backed by a RandomAccessFile.
 * 
 * @author  David Roberts
 */
public class RandomAccessFileHandle extends AbstractFileHandle {
    /** The file */
    private final RandomAccessFile file;
    /**
     * Construct a new instance for the given file, with the given flags.
     * 
     * @param file          the File object representing the file
     * @param flags         the file status flags
     * @throws IOException  if an error occurs while opening the file
     */
    public RandomAccessFileHandle(Context context, File file, int flags)
    throws IOException {
        super(context, (flags & IO.O_WRONLY) == 0,
              (flags & (IO.O_WRONLY|IO.O_RDWR)) != 0,
              (flags & IO.O_SYNC) != 0);
        this.file = new RandomAccessFile(file, this.write ? "rw" : "r");
        if(this.write && (flags & IO.O_TRUNC) != 0)
            this.file.getChannel().truncate(0);
    }
    
    protected int read() throws IOException {
        return file.read();
    }
    
    protected boolean available() throws IOException {
        return file.getFilePointer() < file.length();
    }
    
    protected void write(int b) throws IOException {
        file.write(b);
    }
    
    protected void flush() throws IOException {
        file.getFD().sync();
    }
    
    public void close() throws IOException {
        file.close();
    }
    
    public int seek(int offset, int whence) {
        long n = offset;
        try {
            switch(whence) {
            case IO.SEEK_SET: break;
            case IO.SEEK_CUR: n += file.getFilePointer(); break;
            case IO.SEEK_END: n += file.length(); break;
            default: return error.errno(Error.EINVAL);
            }
            file.seek(n);
        } catch(IOException e) {
            return error.errno(Error.EINVAL);
        }
        if(n > Integer.MAX_VALUE)
            return error.errno(Error.EOVERFLOW);
        return (int) n;
    }
    
    @Override
    public int write(int buf, int count) {
        if(!write)
            return error.errno(Error.EINVAL);
        
        final int result;
        try {
            if (count==0) {
                result = 0;
            } else if (count==1) {
                file.writeByte(memory.load_i8(buf));
                result = 1;
            } else {
                PageWriter writer = new PageWriter();
                memory.getPages(writer, buf, count);
                if (writer.ioExc!=null)
                    throw writer.ioExc;                
                result = writer.count;
            }
            if(synchronous)
                flush();
        } catch(IOException e) {
            return error.errno(Error.EIO);
        }
        return result;
    }
    
    private class PageWriter implements Memory.PageConsumer {
        IOException ioExc;
        int count;
        PageWriter() {}
        @Override
        public boolean next(ByteBuffer buf) {
            while(buf.hasRemaining()) {
                try {
                    count += file.getChannel().write(buf);
                } catch (IOException e) {
                    ioExc = e;
                    return false;
                }
            }
            return true;
        }
    }
    
    @Override
    public int read(int buf, int count) {
        if(!read)
            return error.errno(Error.EINVAL);
        final int result;
        try {
            if (count==0) {
                result = 0;
            } else if (count==1) {
                int val = file.read();
                if (val>=0) {
                    memory.store(buf,(byte)val);
                    result = 1;
                } else {
                    result = 0;
                }
            } else {
                PageReader reader = new PageReader();
                memory.getPages(reader, buf, count);
                if (reader.ioExc!=null)
                    return error.errno(Error.EIO);
                result = reader.count;
            }
        } catch (IOException e) {
            return error.errno(Error.EIO);
        }
        return result;        
    }
    
    class PageReader implements Memory.PageConsumer {
        IOException ioExc;
        int count;
        PageReader() {}
        @Override
        public boolean next(ByteBuffer buf) {
            while(buf.hasRemaining()) {
                try {
                    int r = file.getChannel().read(buf);
                    if (r<0)
                        return false;
                    count += r;
                } catch (IOException e) {
                    ioExc = e;
                    return false;
                }
            }
            return true;
        }
    }
}
