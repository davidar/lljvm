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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import lljvm.util.ClassInfo;
import lljvm.util.ClassName;
import lljvm.util.FieldInfo;
import lljvm.util.MethodInfo;

/**
 * A {@link LinkerPass} that generates a {@link ClassInfo} describing the class of the {@link AsmSource}.
 * @author Joshua Arnold
 *
 */
public class InspectionPass extends LinkerPass<ClassInfo> {
    private static final Logger logger = Logger.getLogger(InspectionPass.class.getName());

    private List<FieldInfo> declaredFields;

    private List<MethodInfo> declaredMethods;

    private List<String> classAttribs;

    private String className;

    InspectionPass(AsmSource file) {
        super(file);
    }

    @Override
    protected void init() throws IOException {
        declaredFields = new ArrayList<FieldInfo>();
        declaredMethods = new ArrayList<MethodInfo>();
        classAttribs = null;
        className = null;
        if (logger.isLoggable(Level.FINE))
            fine("Start Inspection Pass");
    }

    private static List<String> rest(List<String> lst) {
        assert lst.size() > 0;
        return lst.subList(1, lst.size());
    }

    @Override
    protected ClassInfo complete() throws IOException {
        if (className == null)
            throw err("Missing class declaration");
        ClassInfo info = new ClassInfo(ClassName.from(className), declaredMethods, declaredFields,
                classAttribs);
        if (logger.isLoggable(Level.FINE))
            fine("End Inspection Pass; className = " + info.getBinaryName());
        return info;
    }

    @Override
    protected void cleanup() {
        declaredFields = null;
        declaredMethods = null;
        classAttribs = null;
        className = null;
    }

    @Override
    protected void processLine(String line, int lineNum) throws IOException {
        final int len = line.length();
        char c = 0;
        int p;
        for (p = 0; p < len; p++) {
            c = line.charAt(p);
            if (!Character.isWhitespace(c))
                break;
        }
        if (p >= len)
            return;
        if (c != '.')
            return;

        List<String> tokens = words(line.substring(p));
        String instr = tokens.get(0);
        if (".class".equals(instr)) {
            processClassDeclaration(rest(tokens));
        } else if (".field".equals(instr)) {
            processFieldDeclaration(rest(tokens));
        } else if (".method".equals(instr)) {
            processMethodDeclaration(rest(tokens));
        }

    }

    private void processClassDeclaration(List<String> tokens) {
        if (className != null)
            throw err("Duplicate .class directive");
        if (tokens.isEmpty())
            throw err("Invalid class declaration");
        classAttribs = Collections.unmodifiableList(tokens.subList(0, tokens.size() - 1));
        className = tokens.listIterator(tokens.size()).previous();
        if (logger.isLoggable(Level.FINER))
            finer("Processed class declaration: %s", className);

    }

    private void processFieldDeclaration(List<String> tokens) {
        if (className == null)
            throw err("Missing class declaration");

        // Strip initializer
        for (ListIterator<String> li = tokens.listIterator(); li.hasNext();) {
            if (li.next().startsWith("="))
                tokens = tokens.subList(0, li.previousIndex());
        }

        if (tokens.size() < 2)
            throw err("Invalid field declaration");
        ListIterator<String> li = tokens.listIterator(tokens.size());
        String type = li.previous();
        String name = li.previous();
        tokens = tokens.subList(0, tokens.size() - 2);
        FieldInfo fi;
        try {
            fi = new FieldInfo(className + "/" + name + " " + type, tokens);
        } catch (RuntimeException e) {
            throw err("Invalid field declaration").initCause(e);
        }
        declaredFields.add(fi);
        if (logger.isLoggable(Level.FINER))
            finer("Processed field declaration: %s", fi.getQualifiedSignature());

    }

    private void processMethodDeclaration(List<String> tokens) {
        if (className == null)
            throw err("Missing class declaration");

        if (tokens.size() < 1)
            throw err("Invalid field declaration");
        ListIterator<String> li = tokens.listIterator(tokens.size());
        String nameAndType = li.previous();
        tokens = tokens.subList(0, tokens.size() - 1);
        MethodInfo mi;
        try {
            mi = new MethodInfo(className + "/" + nameAndType, tokens);
        } catch (RuntimeException e) {
            throw err("Invalid method declaration").initCause(e);
        }
        declaredMethods.add(mi);
        if (logger.isLoggable(Level.FINER))
            finer("Processed method declaration: %s", mi.getQualifiedSignature());

    }

    private void finer(String msg, Object... args) {
        logger.finer(withLine(String.format(msg, args)));
    }

    private void fine(String msg, Object... args) {
        logger.fine(withLine(String.format(msg, args)));
    }

    /**
     * Breaks a line into whitespace delimited words. 
     */
    private static List<String> words(String line) {
        /*
         * String.split() would be easier, but I've found that getting rid of reg-exps improves
         * the run time of the linker.  This is partly because the linker doesn't usually
         * usually run long enough for the JIT to do much optimization. 
         */
        ArrayList<String> res = new ArrayList<String>(8);
        final int len = line.length();
        int end;
        for (int start = nextNonWhiteSpace(line, 0); start < len; start = nextNonWhiteSpace(line,
                end + 1)) {
            if (line.charAt(start) == ';')
                break;
            end = nextWhiteSpace(line, start + 1);
            res.add(line.substring(start, end));
        }
        return res;
    }
    private static int nextWhiteSpace(String s, int start) {
        final int len = s.length();
        int p = start < 0 ? 0 : start;
        for (; p < len; p++) {
            if (Character.isWhitespace(s.charAt(p)))
                return p;
        }
        return len;
    }

    private static int nextNonWhiteSpace(String s, int start) {
        final int len = s.length();
        int p = start < 0 ? 0 : start;
        for (; p < len; p++) {
            if (!Character.isWhitespace(s.charAt(p)))
                return p;
        }
        return len;
    }


}
