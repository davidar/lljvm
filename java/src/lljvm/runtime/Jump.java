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
    /** The ID of this Jump */
    public final int id;
    /** The return value of this Jump */
    public final int value;
        
    /**
     * Create a new Jump with the given ID and return value.
     * 
     * @param id     the ID
     * @param value  the return value
     */
    Jump(int id, int value) {
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
    

}
