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

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.System;

/**
 * Provides methods and constants related to I/O.
 * 
 * @author  David Roberts
 */
public final class IO {
    /** Open for reading only */
    public static final int O_RDONLY   = 0x0000;
    /** Open for writing only */
    public static final int O_WRONLY   = 0x0001;
    /** Open for reading and writing */
    public static final int O_RDWR     = 0x0002;
    /** Open in append mode */
    public static final int O_APPEND   = 0x0008;
    /** Create the file if it does not already exist */
    public static final int O_CREAT    = 0x0200;
    /** Truncate the file to length 0 if it already exists */
    public static final int O_TRUNC    = 0x0400;
    /** Ensure that this call creates the file */
    public static final int O_EXCL     = 0x0800;
    /** Open for synchronous I/O */
    public static final int O_SYNC     = 0x2000;
    /** Open in non-blocking mode */
    public static final int O_NONBLOCK = 0x4000;
    /** Don't assign a tty on this open */
    public static final int O_NOCTTY   = 0x8000;
    
    /** Set file offset to offset */
    public static final int SEEK_SET = 0;
    /** Set file offset to current plus offset */
    public static final int SEEK_CUR = 1;
    /** Set file offset to EOF plus offset */
    public static final int SEEK_END = 2;
    
    /** The maximum number of files that a process can open */
    public static final int OPEN_MAX = 1<<10;
    
    /** File descriptor table */
    private static final FileHandle[] fileDescriptors =
        new FileHandle[OPEN_MAX];
    /** Number of file descriptors that have been opened */
    private static int numFileDescriptors = 0;
    
    /**
     * Interface for performing operations on a file descriptor.
     */
    public static interface FileHandle extends Closeable {
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
    
    /**
     * A minimal implementation of the FileHandle interface.
     */
    public static abstract class AbstractFileHandle implements FileHandle {
        /** Specifies whether this file descriptor supports reading */
        protected final boolean read;
        /** Specifies whether this file descriptor supports writing */
        protected final boolean write;
        
        /**
         * Construct a new instance with the given read/write capabilities.
         * 
         * @param read   specifies whether this file descriptor supports
         *               reading
         * @param write  specifies whether this file descriptor supports
         *               writing
         */
        protected AbstractFileHandle(boolean read, boolean write) {
            this.read = read;
            this.write = write;
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
    
    /**
     * Implements the FileHandle interface, backed by an InputStream.
     */
    public static class InputStreamFileHandle extends AbstractFileHandle {
        /** The input stream */
        private InputStream inputStream;
        
        /**
         * Construct a new instance with the given input stream.
         * 
         * @param inputStream  the input stream
         */
        public InputStreamFileHandle(InputStream inputStream) {
            super(true, false);
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
    
    /**
     * Implements the FileHandle interface, backed by an OutputStream.
     */
    public static class OutputStreamFileHandle extends AbstractFileHandle {
        /** The output stream */
        private OutputStream outputStream;
        
        /**
         * Construct a new instance with the given output stream.
         * 
         * @param outputStream  the output stream
         */
        public OutputStreamFileHandle(OutputStream outputStream) {
            super(false, true);
            this.outputStream = outputStream;
        }
        
        protected void write(int b) throws IOException {
            outputStream.write(b);
        }
        
        protected void flush() throws IOException {
            outputStream.flush();
        }
        
        public void close() throws IOException {
            outputStream.close();
        }
    }
    
    /**
     * Implements the FileHandle interface, backed by a RandomAccessFile.
     */
    public static class RandomAccessFileHandle extends AbstractFileHandle {
        /** The file */
        private final RandomAccessFile file;
        
        /**
         * Construct a new instance for the given file, with the given flags.
         * 
         * @param name   the name of the file
         * @param flags  the file status flags
         * @throws FileNotFoundException
         *               if an error occurs while opening the file
         */
        public RandomAccessFileHandle(String name, int flags)
        throws FileNotFoundException {
            super((flags & O_WRONLY) == 0, (flags & (O_WRONLY|O_RDWR)) != 0);
            this.file = new RandomAccessFile(name, this.write ? "rw" : "r");
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
                case SEEK_SET: break;
                case SEEK_CUR: n += file.getFilePointer(); break;
                case SEEK_END: n += file.length(); break;
                default: return Error.errno(Error.EINVAL);
                }
                file.seek(n);
            } catch(IOException e) {
                return Error.errno(Error.EINVAL);
            }
            if(n > Integer.MAX_VALUE)
                return Error.errno(Error.EOVERFLOW);
            return (int) n;
        }
    }
    
    /**
     * Prevent this class from being instantiated.
     */
    private IO() {}
    
    static {
        open(new InputStreamFileHandle(System.in));
        open(new OutputStreamFileHandle(System.out));
        open(new OutputStreamFileHandle(System.err));
    }
    
    /**
     * Create a new file descriptor for the given file, with the given flags.
     * 
     * @param name   the name of the file
     * @param flags  the file status flags
     * @return       the new file descriptor on success, -1 on error
     */
    public static int open(String name, int flags) {
        try {
            return open(new RandomAccessFileHandle(name, flags));
        } catch(FileNotFoundException e) {
            return Error.errno(Error.ENOENT);
        }
    }
    
    /**
     * Add the given FileHandle to the file descriptor table.
     * 
     * @param fileHandle  the FileHandle
     * @return            the new file descriptor on success, -1 on error
     */
    private static int open(FileHandle fileHandle) {
        if(numFileDescriptors >= OPEN_MAX)
            return Error.errno(Error.ENFILE);
        int fd = numFileDescriptors++;
        fileDescriptors[fd] = fileHandle;
        return fd;
    }
    
    /**
     * Returns the FileHandle for the given file descriptor.
     * 
     * @param fd  the file descriptor
     * @return    the FileHandle
     */
    public static FileHandle getFileHandle(int fd) {
        return fileDescriptors[fd];
    }
    
    /**
     * Close the given file descriptor.
     * 
     * @param fd  the file descriptor
     * @return    0 on success, -1 on error
     */
    public static int close(int fd) {
        if(fd < 0 || fd >= OPEN_MAX || fileDescriptors[fd] == null)
            return Error.errno(Error.EBADF);
        try {
            fileDescriptors[fd].close();
            fileDescriptors[fd] = null;
        } catch(IOException e) {
            return Error.errno(Error.EIO);
        }
        return 0;
    }
    
    /**
     * Close all open file descriptors, including the standard input, standard
     * output and standard error streams. This method should only be called
     * during an exit.
     */
    public static void close() {
        for(int fd = 0; fd < numFileDescriptors; fd++)
            close(fd);
    }
    
    /**
     * Test whether the given file descriptor refers to a terminal.
     * 
     * @param fd  the file descriptor to test
     * @return    true if fd refers to a terminal
     */
    public static boolean isatty(int fd) {
        if(fd < 0 || fd > 2)
            return false;
        return System.console() != null;
    }
}
