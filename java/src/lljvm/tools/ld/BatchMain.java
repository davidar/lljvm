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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import lljvm.tools.LoggingEnvironment;

/**
 * Main class for executing the linker in "batch" mode.   Multiple ASM files may be processed
 * at once and they may refer to each other.  
 * @author Joshua Arnold
 */
public class BatchMain {
    
    public static int doMain(String[] args, InputStream in, OutputStream out, PrintWriter err) {
        final String pathSep = System.getProperty("path.separator", ":");
        try {
            LinkerParameters params = new LinkerParameters();
            for(String s: args) {
                if (s.startsWith("-u")) {
                    params.setUnresolvedTarget(s.substring(2));
                } else if (s.startsWith("-l")) {
                    params.addLibraryClass(s.substring(2));
                } else {
                    int p = s.indexOf(pathSep);
                    if (p<0)
                        params.addSource(new AsmFileSource(new File(s)));
                    else
                        params.addSource(new AsmFileSource(new File(s.substring(0,p)), new File(s.substring(p+1))));
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
