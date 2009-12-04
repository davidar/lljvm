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

package lljvm.tools.info;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lljvm.util.ReflectionUtils;

/**
 * Main class for executing the LLJVM info utility.
 * 
 * @author  David Roberts
 */
public class Main {
    /**
     * Main method.
     * 
     * @param args  Command line arguments.
     */
    public static void main(String[] args) {
        for(String classname : args) {
            Class<?> cls;
            try {
                cls = ReflectionUtils.getClass(classname);
            } catch (ClassNotFoundException e) {
                System.out.println("Unable to find class " + classname);
                continue;
            }
            System.out.println(classname);
            System.out.println("\nFields");
            for(Field field : ReflectionUtils.getPublicStaticFields(cls))
                System.out.println(ReflectionUtils.getSignature(field));
            System.out.println("\nMethods");
            for(Method method : ReflectionUtils.getPublicStaticMethods(cls))
                System.out.println(ReflectionUtils.getSignature(method));
            System.out.println("\n\n");
        }
    }
}
