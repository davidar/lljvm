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
 * Provides methods for interfacing with the system.
 * 
 * @author  David Roberts
 */
public final class System {
    /**
     * Thrown by the unwind instruction, caught by the invoke instruction.
     */
    @SuppressWarnings("serial")
    public static class Unwind extends RuntimeException {}
    
    /**
     * Thrown at the beginning of any blocks of unreachable code.
     * Therefore, control flow should never reach the point where this
     * exception is thrown.
     */
    @SuppressWarnings("serial")
    public static class Unreachable extends RuntimeException {}

    /**
     * Prevent this class from being instantiated.
     */
    private System() {}
    
    /**
     * Constructs an argument vector from the given arguments.
     * 
     * @param arg0  the first argument i.e. the name of the command being
     *              executed
     * @param args  the rest of the arguments
     * @return      the argument vector
     */
    public static int argv(String arg0, String[] args) {
        final int argc = args.length + 1;
        final int argv = Memory.allocateStack(argc*4);
        Memory.store(argv, Memory.storeStack(arg0));
        for(int i = 1; i < argc; i++)
            Memory.store(argv + i*4, Memory.storeStack(args[i-1]));
        return argv;
    }
    
    /**
     * Performs any necessary cleanup, then terminates with the specified
     * status code.
     * 
     * @param status  the exit status code
     */
    public static void _exit(int status) {
        java.lang.System.exit(status);
    }
    
    /**
     * Close the specified file descriptor.
     * 
     * @param fd  the file descriptor to close
     * @return    0 on success, -1 on error
     */
    public static int close(int fd) {
        return -1;
    }
    
    /**
     * Execute the program pointed to by filename.
     * 
     * @param filename  the name of the executable
     * @param argv      the argument vector to be passed to the new program
     * @param envp      an array of environment variables
     * @return          does not return on success, -1 on error
     */
    public static int execve(int filename, int argv, int envp) {
        Error.errno(Error.ENOMEM);
        return -1;
    }
    
    /**
     * Duplicate the calling process.
     * 
     * @return  the PID of the child process on success, -1 on error
     */
    public static int fork() {
        Error.errno(Error.EAGAIN);
        return -1;
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
     * Returns to process ID of the calling process.
     * 
     * @return  the PID of the calling process
     */
    public static int getpid() {
        return 1;
    }
    
    /**
     * Test whether the given file descriptor refers to a terminal.
     * 
     * @param fd  the file descriptor to test
     * @return    1 if fd refers to a terminal, 0 otherwise
     */
    public static int isatty(int fd) {
        return 1;
    }
    
    /**
     * Send a signal to a process.
     * 
     * @param pid  the PID of the process
     * @param sig  the signal
     * @return     0 on success, -1 on error
     */
    public static int kill(int pid, int sig) {
        Error.errno(Error.EINVAL);
        return -1;
    }
    
    /**
     * Create a new (hard) link to an existing file.
     * 
     * @param oldpath  the existing file
     * @param newpath  the link to be created, unless newpath already exists
     * @return         0 on success, -1 on error
     */
    public static int link(int oldpath, int newpath) {
        Error.errno(Error.EMLINK);
        return -1;
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
        return 0;
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
        if((flags & IO.O_CREAT) != 0)
            return _open(pathname, flags, Memory.load_i32(args));
        else
            return _open(pathname, flags);
    }
    
    /**
     * Open a file or device.
     * 
     * @param pathname  the pathname of the file
     * @param flags     specifies the access mode
     * @return          the new file descriptor on success, -1 on error
     */
    private static int _open(int pathname, int flags) {
        return -1;
    }
    
    /**
     * Open and create a file or device.
     * 
     * @param pathname  the pathname of the file
     * @param flags     specifies the access mode
     * @param mode      the permissions for the newly created file
     * @return          the new file descriptor on success, -1 on error
     */
    private static int _open(int pathname, int flags, int mode) {
        return -1;
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
        return 0;
    }
    
    /**
     * Increase the size of the heap by the specified amount.
     * 
     * @param increment  the amount to increment the heap size
     * @return           a pointer to the previous end of the heap on success,
     *                   -1 on error
     */
    public static int sbrk(int increment) {
        return Memory.sbrk(increment);
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
     * Get process times.
     * 
     * @param buf  a pointer to the tms structure the process times are to be
     *             stored in
     * @return     the number of clock ticks that have elapsed since an
     *             arbitrary point in the past on success, -1 on error
     */
    public static int times(int buf) {
        return -1;
    }
    
    /**
     * Delete a name from the file system.
     * 
     * @param pathname  the name to delete
     * @return          0 on success, -1 on error
     */
    public static int unlink(int pathname) {
        Error.errno(Error.ENOENT);
        return -1;
    }
    
    /**
     * Wait for a process to change state.
     * 
     * @param status  a pointer to the int in which status information is to be
     *                stored
     * @return        the PID of the terminated child on success, -1 on error
     */
    public static int wait(int status) {
        Error.errno(Error.ECHILD);
        return -1;
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
        for(int i = 0; i < count; i++)
            java.lang.System.out.write(Memory.load_i8(buf++));
        return count;
    }
}
