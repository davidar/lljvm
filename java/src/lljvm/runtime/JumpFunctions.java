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
 * Module containing the {@code setjmp} and {@code longjmp} functions.
 * @author David Roberts (wrote the original code contained in the Jump class)
 * @author Joshua Arnold (refactored into this class)
 *
 */
public class JumpFunctions implements Module {
	
    /** The number of setjmp calls that have been made so far */
    private int numJumps = 0;

    private Memory memory;
    
    /**
     * Create a new Jump with the given ID and return value.
     * 
     * @param id     the ID
     * @param value  the return value
     */
    public JumpFunctions() {
    }

    @Override
    public void initialize(Context context) {
        this.memory = context.getModule(Memory.class);
    }

    @Override
    public void destroy(Context context) {
    }

    /**
     * Save the stack context in env for later use by longjmp.
     * 
     * @param env  where to store the stack context
     * @return     the unique ID of this jump target
     */
    public int setjmp(int env) {
        int id = ++numJumps;
        int stackDepth = memory.getStackDepth();
        memory.store(env, id);
        memory.store(env+4, stackDepth);
        return id;
    }
    
    /**
     * Jump to the last call of setjmp with the corresponding env argument,
     * causing setjmp to return the given value.
     * If the return value is 0, 1 will be returned instead.
     * 
     * @param env  the stack context stored by setjmp
     * @param val  the return value
     */
    public void longjmp(int env, int val) {
        int id = memory.load_i32(env);
        int stackDepth = memory.load_i32(env+4);
        memory.destroyStackFrames(memory.getStackDepth() - stackDepth);
        throw new Jump(id, val);
    }
}
