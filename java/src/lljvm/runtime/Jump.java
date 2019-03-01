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
 * Provides support for setjmp/longjmp.
 * 
 * @author  David Roberts
 */
@SuppressWarnings("serial")
public class Jump extends RuntimeException {
    /** The number of setjmp calls that have been made so far */
    private static int numJumps = 0;
    
    /** The ID of this Jump */
    public final int id;
    /** The return value of this Jump */
    public final int value;
    
    private Environment env;
    
    /**
     * Create a new Jump with the given ID and return value.
     * 
     * @param id     the ID
     * @param value  the return value
     */
    private Jump(Environment env, int id, int value) {
        this.env = env;
        this.id = id;
        this.value = (value == 0 ? 1 : value);
    }
    
    /**
     * Disable Throwable.fillInStackTrace as it is costly and unnecessary for
     * this purpose.
     */
    public Throwable fillInStackTrace() {
        return this;
    }
    
    /**
     * Save the stack context in env for later use by longjmp.
     * 
     * @param env  the execution environment
     * @param env_  where to store the stack context
     * @return     the unique ID of this jump target
     */
    public static int setjmp(Environment env, int env_) {
        int id = ++numJumps;
        int stackDepth = env.memory.getStackDepth();
        env.memory.store(env_, id);
        env.memory.store(env_+4, stackDepth);
        return id;
    }
    
    /**
     * Jump to the last call of setjmp with the corresponding env argument,
     * causing setjmp to return the given value.
     * If the return value is 0, 1 will be returned instead.
     * 
     * @param env  the execution environment
     * @param env_ the stack context stored by setjmp
     * @param val  the return value
     */
    public static void longjmp(Environment env, int env_, int val) {
        int id = env.memory.load_i32(env_);
        int stackDepth = env.memory.load_i32(env_+4);
        env.memory.destroyStackFrames(env.memory.getStackDepth() - stackDepth);
        throw new Jump(env, id, val);
    }
}
