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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import lljvm.tools.LoggingEnvironment;

/**
 * Main class for executing the LLJVM linker.
 * 
 * @author David Roberts
 * @author Joshua Arnold
 */
public class Main {
    
    public static int doMain(String[] args, InputStream in, OutputStream out, PrintWriter err) {
        try {
            LinkerParameters params = new LinkerParameters();
            params.addSource(new AsmStreamSource("<stdin>", in, out));
            for (String arg : args) {
                if (arg.startsWith("~")) {
                    params.setUnresolvedTarget(arg.substring(1));
                } else {
                    params.addLibraryClass(arg);
                }
            }
            AsmLinker linker = new AsmLinker(params);
            linker.run();
        } catch (AsmLinkerException e) {
            err.println("Linker Error: " + e);
            e.printStackTrace(err);
            err.flush();
            return 1;            
        }
        return 0;
    }
    


    public static void main(String[] args)  {
        args = LoggingEnvironment.setupLogging(args).getNonLoggingArgs(); 
        int res;
        try {
            res = doMain(args, System.in, System.out, new PrintWriter(System.err));
        } catch (Throwable t) {
            System.err.println("An unexpected error occurred: " + t);
            t.printStackTrace();
            res = 1;            
        }
        System.exit(res);        
    }
    
    
}
