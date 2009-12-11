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

import java.io.File;
import java.io.IOException;
import java.lang.System;

import lljvm.io.FileHandle;
import lljvm.io.InputStreamFileHandle;
import lljvm.io.OutputStreamFileHandle;
import lljvm.io.RandomAccessFileHandle;

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
    
    /** Read permission, owner */
    public static final int S_IRUSR = 0000400;
    /** Write permission, owner */
    public static final int S_IWUSR = 0000200;
    /** Execute/search permission, owner */
    public static final int S_IXUSR = 0000100;
    /** All permissions, owner */
    public static final int S_IRWXU = (S_IRUSR | S_IWUSR | S_IXUSR);
    /** Read permission, group */
    public static final int S_IRGRP = 0000040;
    /** Write permission, group */
    public static final int S_IWGRP = 0000020;
    /** Execute/search permission, group */
    public static final int S_IXGRP = 0000010;
    /** All permissions, group */
    public static final int S_IRWXG = (S_IRGRP | S_IWGRP | S_IXGRP);
    /** Read permission, others */
    public static final int S_IROTH = 0000004;
    /** Write permission, others */
    public static final int S_IWOTH = 0000002;
    /** Execute/search permission, others */
    public static final int S_IXOTH = 0000001;
    /** All permissions, others */
    public static final int S_IRWXO = (S_IROTH | S_IWOTH | S_IXOTH);
    
    /** The maximum number of files that a process can open */
    public static final int OPEN_MAX = 1<<10;
    
    /** File descriptor table */
    private static final FileHandle[] fileDescriptors =
        new FileHandle[OPEN_MAX];
    /** Number of file descriptors that have been opened */
    private static int numFileDescriptors = 0;
    
    static {
        open(new InputStreamFileHandle(System.in));
        open(new OutputStreamFileHandle(System.out));
        open(new OutputStreamFileHandle(System.err));
    }
    
    /** User working directory system property */
    private static final String USER_DIR = System.getProperty("user.dir");
    /** Current working directory */
    private static File cwd = USER_DIR == null ? null : new File(USER_DIR);
    
    /**
     * Prevent this class from being instantiated.
     */
    private IO() {}
    
    /**
     * Return the File object representing the file with the given name,
     * relative to the current working directory if applicable.
     * 
     * @param name  the name of the file
     * @return      the File object representing the file
     */
    private static File newFile(String name) {
        File file = new File(name);
        if(!file.isAbsolute())
            file = new File(cwd, name);
        return file;
    }
    
    /**
     * Open and possibly create a file or device.
     * 
     * @param pathname  the pathname of the file
     * @param flags     specifies the access mode
     * @param args      a pointer to the packed list of varargs
     *                  i.e. a pointer to the mode argument, the permissions
     *                  for the newly created file (if applicable)
     * @return          the new file descriptor on success, -1 on error
     */
    public static int open(int pathname, int flags, int args) {
        File file = newFile(Memory.load_string(pathname));
        return ((flags & O_CREAT) != 0)
            ? open(file, flags, Memory.load_i32(args))
            : open(file, flags);
    }
    
    /**
     * Create and open a file or device.
     * 
     * @param file   the File object representing the file
     * @param flags  specifies the access mode
     * @param mode   the permissions for the newly created file
     * @return       the new file descriptor on success, -1 on error
     */
    private static int open(File file, int flags, int mode) {
        try {
            if(file.createNewFile()) {
                file.setReadable(  (mode & (S_IRUSR|S_IRGRP|S_IROTH)) != 0,
                                   (mode & (S_IRGRP|S_IROTH)) == 0);
                file.setWritable(  (mode & (S_IWUSR|S_IWGRP|S_IWOTH)) != 0,
                                   (mode & (S_IWGRP|S_IWOTH)) == 0);
                file.setExecutable((mode & (S_IXUSR|S_IXGRP|S_IXOTH)) != 0,
                                   (mode & (S_IXGRP|S_IXOTH)) == 0);
            } else { // file already exists
                if((flags & O_EXCL) != 0)
                    return Error.errno(Error.EEXIST);
            }
        } catch(IOException e) {
            return Error.errno(Error.EACCES);
        }
        return open(file, flags);
    }
    
    /**
     * Create a new file descriptor for the given file, with the given flags.
     * 
     * @param file   the File object representing the file
     * @param flags  the file status flags
     * @return       the new file descriptor on success, -1 on error
     */
    private static int open(File file, int flags) {
        try {
            return open(new RandomAccessFileHandle(file, flags));
        } catch(IOException e) {
            return Error.errno(Error.EACCES);
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
    private static FileHandle getFileHandle(int fd) {
        return fileDescriptors[fd];
    }
    
    /**
     * Read from a file descriptor.
     * 
     * @param fd     the file descriptor to be read
     * @param buf    the buffer to read the bytes into
     * @param count  the maximum number of bytes to read
     * @return       the number of bytes read on success, -1 on error
     */
    public static int read(int fd, int buf, int count) {
        return getFileHandle(fd).read(buf, count);
    }
    
    /**
     * Write to a file descriptor.
     * 
     * @param fd     the file descriptor to be written to
     * @param buf    the buffer of bytes to be written
     * @param count  the maximum number of bytes to write
     * @return       the number of bytes written on success, -1 on error
     */
    public static int write(int fd, int buf, int count) {
        return getFileHandle(fd).write(buf, count);
    }
    
    /**
     * Reposition file offset.
     * 
     * @param fd      the file descriptor whose offset to reposition
     * @param offset  where to reposition the offset according to the directive
     *                whence
     * @param whence  specifies the reference point to which offset refers
     * @return        the resulting offset on success, -1 on error
     */
    public static int lseek(int fd, int offset, int whence) {
        return getFileHandle(fd).seek(offset, whence);
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
     * @return    1 if fd refers to a terminal, 0 otherwise
     */
    public static int isatty(int fd) {
        if(fd < 0 || fd > 2)
            return 0;
        return System.console() != null ? 1 : 0;
    }
    
    /**
     * Create a new (hard) link to an existing file.
     * 
     * @param oldpath  the existing file
     * @param newpath  the link to be created, unless newpath already exists
     * @return         0 on success, -1 on error
     */
    public static int link(int oldpath, int newpath) {
        // TODO: Java 7: <http://java.sun.com/docs/books/tutorial/essential/
        //                                              io/links.html#hardLink>
        return Error.errno(Error.EMLINK);
    }
    
    /**
     * Delete a name from the file system.
     * 
     * @param pathname  the name to delete
     * @return          0 on success, -1 on error
     */
    public static int unlink(int pathname) {
        return Error.errno(Error.ENOENT);
    }
    
    /**
     * Stats the file pointed to by path and fills in buf.
     * 
     * @param path  the path of the file to be stat-ed
     * @param buf   a pointer to the stat structure to be filled in
     * @return      0 on success, -1 on error
     */
    public static int stat(int path, int buf) {
        // TODO: st->st_mode = S_IFCHR;
        return 0;
    }
    
    /**
     * Stats the file specified by the given file descriptor and fills in buf.
     * 
     * @param fd   the file descriptor to be stat-ed
     * @param buf  a pointer to the stat structure to be filled in
     * @return     0 on success, -1 on error
     */
    public static int fstat(int fd, int buf) {
        // TODO: buf->st_mode = S_IFCHR;
        return 0;
    }
    
    /**
     * Change the name or location of a file.
     * 
     * @param oldpath  the current path of the file
     * @param newpath  the new path of the file
     * @return         0 on success, -1 on error
     */
    public static int _rename(int oldpath, int newpath) {
        File oldfile = newFile(Memory.load_string(oldpath));
        File newfile = newFile(Memory.load_string(newpath));
        if(!oldfile.renameTo(newfile))
            return Error.errno(Error.EACCES);
        return 0;
    }
    
    /**
     * Change the working directory.
     * 
     * @param path  the new working directory
     * @return      0 on success, -1 on error
     */
    public static int chdir(int path) {
        File newCWD = newFile(Memory.load_string(path));
        if(!newCWD.exists())
            return Error.errno(Error.ENOENT);
        cwd = newCWD;
        return 0;
    }
    
    /**
     * Copy the absolute pathname of the current working directory into the
     * given buffer
     * 
     * @param buf   the result buffer
     * @param size  the size of the buffer
     * @return      buf on success, NULL on error
     */
    public static int getcwd(int buf, int size) {
        return Memory.store(buf, cwd.getAbsolutePath(), size);
    }
}
