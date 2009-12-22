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

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lljvm.util.ReflectionUtils;

/**
 * Main class for executing the LLJVM info utility.
 * 
 * @author  David Roberts
 */
public class Main {
    /** Display fields/methods beginning with an underscore? */
    private static boolean verbose = false;
    
    /**
     * Print the given list of fields or methods under the given header.
     * 
     * @param header  the header
     * @param list    the list of fields or methods
     */
    private static void printList(String header,
                                  List<? extends AccessibleObject> list) {
        System.out.println();
        System.out.println(header);
        List<String> sigs = new ArrayList<String>();
        for(AccessibleObject o : list) {
            String sig = ReflectionUtils.getSignature(o);
            if(verbose || sig.charAt(0) != '_')
                sigs.add(sig);
        }
        Collections.sort(sigs);
        for(String sig : sigs)
            System.out.println(sig);
    }
    
    /**
     * Main method.
     * 
     * @param args  Command line arguments.
     */
    public static void main(String[] args) {
        if(args[0].equals("-v")) {
            verbose = true;
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        for(String classname : args) {
            Class<?> cls;
            try {
                cls = ReflectionUtils.getClass(classname);
            } catch (ClassNotFoundException e) {
                System.out.println("Unable to find class " + classname);
                continue;
            }
            System.out.println(classname);
            printList("Fields",  ReflectionUtils.getPublicStaticFields(cls));
            printList("Methods", ReflectionUtils.getPublicStaticMethods(cls));
            System.out.print("\n\n\n");
        }
    }
}
