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
     * Prevent this class from being instantiated.
     */
    private System() {}
    
    /**
     * Constructs an argument vector from the given arguments.
     * 
     * @param args  the array of arguments
     * @return      the argument vector
     */
    public static int argv(String[] args) {
        final int argc = args.length;
        final int argv = Memory.allocateStack((argc+1)*4);
        for(int i = 0; i < argc; i++)
            Memory.store(argv + i*4, Memory.storeStack(args[i]));
        Memory.store(argv + argc*4, Memory.NULL);
        return argv;
    }
    
    /**
     * Performs any necessary cleanup, then terminates with the specified
     * status code.
     * 
     * @param status  the exit status code
     */
    public static void _exit(int status) {
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
        return Error.errno(Error.ENOMEM);
    }
    
    /**
     * Duplicate the calling process.
     * 
     * @return  the PID of the child process on success, -1 on error
     */
    public static int fork() {
        return Error.errno(Error.EAGAIN);
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
     * Send a signal to a process.
     * 
     * @param pid  the PID of the process
     * @param sig  the signal
     * @return     0 on success, -1 on error
     */
    public static int kill(int pid, int sig) {
        return Error.errno(Error.EINVAL);
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
     * Wait for a process to change state.
     * 
     * @param status  a pointer to the int in which status information is to be
     *                stored
     * @return        the PID of the terminated child on success, -1 on error
     */
    public static int wait(int status) {
        return Error.errno(Error.ECHILD);
    }
}
