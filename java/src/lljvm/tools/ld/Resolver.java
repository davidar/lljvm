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

package lljvm.tools.ld;


import lljvm.util.ClassInfo;

/**
 * Resolves method and field symbolic references to actual methods/fields.
 * @author Joshua Arnold
 */
public interface Resolver {
 
    /**
     * Resolves an external method with the specified symbolic reference.
     * @param signature the method signature (may be qualified or unqualified)
     * @return a {@link MethodReference} describe the resolved method.
     * @throws AsmLinkerException if the resolution fails.
     */
    MethodReference resolveMethod(String signature, ClassInfo referrer);
    
    /**
     * Resolves a local method with the specified symbolic reference.
     * @param signature the method signature (may be qualified or unqualified)
     * @param className the name of the class being linked.
     * @return a {@link MethodReference} describe the resolved method.
     * @throws AsmLinkerException if the resolution fails.
     */
    MethodReference resolveLocalMethod(String signature, ClassInfo referrer);

    /**
     * Resolves an external field with the specified symbolic reference.
     * @param signature the field signature (may be qualified or unqualified)
     * @return a {@link MethodReference} describe the resolved method.
     * @throws AsmLinkerException if the resolution fails.
     */
    FieldReference resolveField(String signature, ClassInfo referrer);

    /**
     * Resolves a local field with the specified symbolic reference.
     * @param signature the field signature (may be qualified or unqualified)
     * @param className the name of the class being linked.
     * @return a {@link MethodReference} describe the resolved method.
     * @throws AsmLinkerException if the resolution fails.
     */
    FieldReference resolveLocalField(String signature, ClassInfo referrer);
    
    
}



