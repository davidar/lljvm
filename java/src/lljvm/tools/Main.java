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

package lljvm.tools;

import java.util.Arrays;

/**
 * Main class for executing the LLJVM tools.
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
        if(args.length < 1) {
            System.err.println("Missing command name");
            System.exit(1);
        }
        String cmd = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);
        if(cmd.equals("ld"))
            lljvm.tools.ld.Main.main(args);
        else if(cmd.equals("info"))
            lljvm.tools.info.Main.main(args);
        else {
            System.err.println("Unrecognised command name");
            System.exit(1);
        }
    }
}
