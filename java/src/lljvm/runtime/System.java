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

// TODO: proper environ support

/**
 * Provides methods for interfacing with the system.
 * 
 * @author  David Roberts
 */
public final class System {
    /** Throw an exception instead of calling System.exit? */
    public static boolean throwExit = false;
    
    /**
     * Thrown to indicate that a call has been made to the _exit syscall.
     */
    @SuppressWarnings("serial")
    public static class Exit extends RuntimeException {
        /** Exit status code */
        public final int status;
        public Exit(int status) {
            super(Integer.toString(status));
            this.status = status;
        }
    }
    
    /**
     * Prevent this class from being instantiated.
     */
    private System() {}
    
    /**
     * Performs any necessary cleanup, then terminates with the specified
     * status code.
     * 
     * @param status  the exit status code
     */
    public static void _exit(int status) {
        if(throwExit)
            throw new Exit(status);
        IO.close();
        java.lang.System.exit(status);
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
        // TODO: implement
        return Error.errno(Error.ENOMEM);
    }
    
    /**
     * Duplicate the calling process.
     * 
     * @return  the PID of the child process on success, -1 on error
     */
    public static int fork() {
        // TODO: implement
        return Error.errno(Error.EAGAIN);
    }
    
    /**
     * Returns to process ID of the calling process.
     * 
     * @return  the PID of the calling process
     */
    public static int getpid() {
        // TODO: implement
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
        // TODO: implement
        return Error.errno(Error.EINVAL);
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
        // TODO: implement
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
        // TODO: implement
        return Error.errno(Error.ECHILD);
    }
}
