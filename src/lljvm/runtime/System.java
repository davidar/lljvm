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
}
