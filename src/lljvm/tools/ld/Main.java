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

package lljvm.tools.ld;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lljvm.util.ReflectionUtils;

/**
 * Main class for executing the LLJVM linker.
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
        List<String> libs = Arrays.asList(args);
        Map<String, String> methodMap = null;
        Map<String, String> fieldMap = null;
        try {
            methodMap = ReflectionUtils.buildMethodMap(libs);
            fieldMap = ReflectionUtils.buildFieldMap(libs);
        } catch(ClassNotFoundException e) {
            System.err.println("Unable to find library");
            e.printStackTrace();
            System.exit(1);
        }
        
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(System.out));
        AsmLinker asm = new AsmLinker(in, out);
        try {
            asm.link(methodMap, fieldMap);
            in.close();
            out.close();
        } catch(IOException e) {
            System.err.println("Error linking file");
            e.printStackTrace();
            System.exit(1);
        } catch(LinkError e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
