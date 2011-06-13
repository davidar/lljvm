/*
 * Copyright (c) 2011 Joshua Arnold
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
 * Defines lifecycle methods for modules.
 * <p>
 * Modules need not implement this interface if they do not require the lifecycle callbacks.
 * </p>
 * @author Joshua Arnold
 *
 */
public interface Module {
    /**
     * Called when a module instance is first associated with a {@link Context}.  Typically, modules 
     * use this method to save a reference to the context.  As an optimization, modules also commonly
     * use this method to obtain references to modules they depend on and save those references for
     * later use. 
     * @param context
     */
    void initialize(Context context);
    
    /**
     * Called when the context associated with the module is destroyed.
     * @param context
     */
    void destroy(Context context);
}
