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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for linking assembly files.
 * 
 * @author  David Roberts
 */
public class AsmLinker {
    /** The reader to read input assembly code */
    private BufferedReader in;
    /** The writer to write linked assembly code */
    private BufferedWriter out;
    
    /** External fields */
    private List<String> externFields = new ArrayList<String>();
    /** External methods */
    private List<String> externMethods = new ArrayList<String>();
    
    /**
     * Construct a new AsmLinker with the specified input and output sources.
     * 
     * @param in   the reader to read input assembly code
     * @param out  the writer to write linked assembly code
     */
    public AsmLinker(BufferedReader in, BufferedWriter out) {
        this.in = in;
        this.out = out;
    }
    
    /**
     * Read external reference directives from the input reader.
     * 
     * @throws IOException  if there is a problem reading or writing
     */
    private void readExtern() throws IOException {
        String line;
        while((line = in.readLine()) != null) {
            String[] args = line.trim().split("\\s+");
            if(args[0].equals(".extern")) {
                if(args[1].equals("field"))
                    externFields.add(args[2] + " " + args[3]);
                else if(args[1].equals("method"))
                    externMethods.add(args[2]);
                out.write(';');
            }
            out.write(line);
            out.write('\n');
            if(args[0].equals(".method"))
                break;
        }
    }
    
    /**
     * Print an invokestatic instruction for the given method.
     * 
     * @param methodName    Operand of the instruction.
     * @param methodMap     Mapping of external methods to classes.
     * @throws IOException  if there is a problem reading or writing
     * @throws LinkError    if there is a problem linking external references
     */
    private void printInvokeStatic(String methodName,
                                   Map<String, String> methodMap)
    throws IOException, LinkError {
        if(!methodName.contains("(")) // non-prototyped function
            for(String name : methodMap.keySet())
                if(name.startsWith(methodName + "(")) {
                    // TODO: throw error unless specified otherwise
                    System.err.println(
                            "WARNING: Function '" + methodName + "' should " +
                            "be declared with a prototype. Linking will " +
                            "succeed, but a runtime error will be thrown.");
                    out.write("\tinvokestatic ");
                    out.write(methodMap.get(name));
                    out.write("/__non_prototyped__");
                    out.write(methodName);
                    out.write("()V\n");
                    return;
                }
        String className = methodMap.get(methodName);
        if(className == null)
            throw new LinkError(
                    "Unable to find external method " + methodName);
        out.write("\tinvokestatic ");
        out.write(className);
        out.write('/');
        out.write(methodName);
        out.write('\n');
    }
    
    /**
     * Print an getstatic instruction for the given method.
     * 
     * @param fieldName     Operand of the instruction.
     * @param fieldMap      Mapping of external fields to classes.
     * @throws IOException  if there is a problem reading or writing
     * @throws LinkError    if there is a problem linking external references
     */
    private void printGetStatic(String fieldName,
                                Map<String, String> fieldMap)
    throws IOException, LinkError {
        String className = fieldMap.get(fieldName);
        if(className == null)
            throw new LinkError(
                    "Unable to find external field " + fieldName);
        out.write("\tgetstatic ");
        out.write(className);
        out.write('/');
        out.write(fieldName);
        out.write('\n');
    }
    
    /**
     * Print an ldc instruction to load the binary name of the parent class
     * of the given method.
     * 
     * @param methodName    the method whose parent class to load
     * @param methodMap     Mapping of external methods to classes.
     * @throws IOException  if there is a problem reading or writing
     * @throws LinkError    if there is a problem linking external references
     */
    private void printClassForMethod(String methodName,
                                     Map<String, String> methodMap)
    throws IOException, LinkError {
        String className = methodMap.get(methodName);
        if(className == null)
            throw new LinkError(
                    "Unable to find external method " + methodName);
        out.write("\tldc \"");
        out.write(className);
        out.write("\"\n");
    }
    
    /**
     * Link references to static methods and fields in the input to the
     * classes specified in the given maps.
     * 
     * @param methodMap     Mapping of external methods to classes.
     * @param fieldMap      Mapping of external fields to classes.
     * @throws IOException  if there is a problem reading or writing
     * @throws LinkError    if there is a problem linking external references
     */
    private void linkStatic(Map<String, String> methodMap,
                            Map<String, String> fieldMap)
    throws IOException, LinkError {
        String line;
        while((line = in.readLine()) != null) {
            String[] args = line.trim().split("\\s+");
            if(args[0].equals("invokestatic")
                    && externMethods.contains(args[1]))
                printInvokeStatic(args[1], methodMap);
            else if(args[0].equals("getstatic")
                    && externFields.contains(args[1] + " " + args[2]))
                printGetStatic(args[1] + " " + args[2], fieldMap);
            else if(args[0].equals("CLASSFORMETHOD")
                    && externMethods.contains(args[1]))
                printClassForMethod(args[1], methodMap);
            else
                out.write(line + '\n');
        }
    }
    
    /**
     * Link references to methods and fields in the input to the classes
     * specified in the given maps.
     * 
     * @param methodMap     Mapping of external methods to classes.
     * @param fieldMap      Mapping of external fields to classes.
     * @throws IOException  if there is a problem reading or writing
     * @throws LinkError    if there is a problem linking external references
     */
    public void link(Map<String, String> methodMap,
                     Map<String, String> fieldMap)
    throws IOException, LinkError {
        readExtern();
        linkStatic(methodMap, fieldMap);
    }
}
