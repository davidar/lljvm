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

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * Main class for executing the LLJVM linker.
 * 
 * @author David Roberts
 * @author Joshua Arnold
 */
public class Main {
	/**
	 * Main method.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static int doMain(String[] args, Reader in, Writer _out, Writer _err) {
		final PrintWriter out = new PrintWriter(new BufferedWriter(_out));
		final PrintWriter err = new PrintWriter(new BufferedWriter(_err));
		try {
			List<String> libs = new ArrayList<String>(Arrays.asList(args));
			String unresolvedClassName = null;
			String inputFile = null;
			for (Iterator<String> iter = libs.iterator(); iter.hasNext();) {
				String item = iter.next();
				if (item.startsWith("~")) {
					unresolvedClassName = item.substring(1);
					iter.remove();
				} else if (item.equals("--input")) {
				    iter.remove();
				    if (iter.hasNext()) {
				        inputFile = iter.next();
				        iter.remove();
				    }
				}
			}
			
			try {
			    Reader linkIn;
			    if (inputFile!=null)
			        linkIn = new InputStreamReader(new FileInputStream(inputFile));  //TODO - Char encoding
			    else
			        linkIn = noClose(in);
	            try {
    	            Resolver resolver = new DefaultResolver(libs,unresolvedClassName,Main.class.getClassLoader());
    	            AsmLinker linker = new AsmLinker(new LineNumberReader(linkIn), out, resolver);
    
    	            linker.link();
	            } finally {
	                linkIn.close();
	            }
			} catch (AsmLinkerException e) {
			    err.println("Linker Error: "+e);
			    e.printStackTrace(err);
			    return 1;
			} catch (IOException e) {
			    err.println("IO Error: "+e);
			    e.printStackTrace(err);
			    return 1;
			}
			
		} finally {
			try {
				out.flush();
			} finally {
				err.flush();
			}
		}
		if (out.checkError()) {
			err.println("Error writing to output stream");
			err.flush();
			return 1;
		}
		if (err.checkError()) {
			err.println("Error writing to error stream");
			err.flush();
			return 1;
		}
		return 0;

	}
	
	private static Reader noClose(Reader in) {
	    return new FilterReader(in) {
            @Override public void close() {}
        };
	}

	public static void main(String[] args) {
		int res;
		try {
			//TODO: Character encodings
			res = doMain(args,new InputStreamReader(System.in),new OutputStreamWriter(System.out), new OutputStreamWriter(System.err));
		} catch (Throwable t) {
			System.err.println("An unexpected error occurred: "+t);
			t.printStackTrace();
			res = 1;
		}
		System.exit(res);
	}
	
}
