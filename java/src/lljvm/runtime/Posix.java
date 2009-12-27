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
 * Provides routines defined by the POSIX API.
 * 
 * @author  David Roberts
 */
public final class Posix {
    /**
     * Prevent this class from being instantiated.
     */
    private Posix() {}
    
    /**
     * Manipulate the given file descriptor.
     * 
     * @param fd    the file descriptor to manipulate
     * @param cmd   specifies the operation to perform
     * @param args  a pointer to the packed list of varargs
     *              i.e. a pointer to the arg argument (if applicable)
     * @return      the appropriate value on success, -1 on error
     */
    public static int fcntl(int fd, int cmd, int args) {
        // TODO: implement
        return Error.errno(Error.EACCES);
    }
    
    /**
     * Get the time.
     * 
     * @param tv  a pointer to the timeval structure to set
     * @param tz  a pointer to the timezone structure to set
     * @return    0 on success, -1 on error
     */
    public static int gettimeofday(int tv, int tz) {
        // TODO: implement
        return Error.errno(Error.EINVAL);
    }
    
    /**
     * Examine and change blocked signals.
     * 
     * @param how     specifies the behaviour of the call
     * @param set     specifies how to change the blocked signals
     * @param oldset  where to store the previous value of the signal mask
     * @return        0 on success, -1 on error
     */
    public static int sigprocmask(int how, int set, int oldset) {
        // TODO: implement
        return Error.errno(Error.EINVAL);
    }
    
    /**
     * Get configuration information.
     * 
     * @param name  the name of the variable to retrieve
     * @return      the value of the system resource on success, -1 on error
     */
    public static int sysconf(int name) {
        // TODO: implement
        return Error.errno(Error.EINVAL);
    }
}
