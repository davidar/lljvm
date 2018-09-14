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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import lljvm.runtime.Context;
import lljvm.util.ClassInfo;

/**
 * A {@link LinkerPass} that writes the final linked ASM source to its output stream.  Note that the return value
 * of the pass is always {@code null}.
 * @author Joshua Arnold
 *
 */
class WriterPass extends LinkerPass<Object> {
    
    private static final Logger logger = Logger.getLogger(WriterPass.class.getName());
    
    private static final int BUFSZ = 1<<15;
    
    private final ClassInfo classInfo;
    private final ResolvedTargets targets;
    
    private final String lineSep = System.getProperty("line.separator", "\n");
    
    private Writer out;
    private Formatter fout;
    
    public WriterPass(AsmSource file, ClassInfo classInfo, ResolvedTargets targets) {
        super(file);
        this.classInfo = classInfo;
        this.targets = targets;
    }

    @Override
    protected void init() throws IOException {
        if (logger.isLoggable(Level.FINE))
            fine("Start Writer Pass");
        out = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(file.startOutput(),BUFSZ), "UTF-8"));
        fout = new Formatter(out);
    }

    @Override
    protected Object complete() throws IOException {
        if (out!=null) {
            out.flush();
            out.close();
            out = null;
            fout = null;
        }
        if (logger.isLoggable(Level.FINE))
            fine("End Writer Pass");
        return null;
    }
    
    @Override
    protected void cleanup() throws IOException {
        
        if (out!=null) {
            try {
                try {
                    out.flush();
                } finally {
                    out.close();
                }
            } finally {
                out = null;
                fout = null;
            }
        }
    }
    
    @Override
    protected void processLine(String line, int lineNum) throws IOException {
        Directive directive = Directive.parse(line);
        if (directive==null) {
            writeln(line);
            return;
        }
        if (TAG_LINKER_HEADER.equals(directive.name)) {
            //TODO - Header no loner needed...
            //processHeader(directive, lineNum);            
        } else if (TAG_INVOKE_BEGIN.equals(directive.name)) {
            processInvokeBegin(directive, lineNum);
        } else if (TAG_INVOKE_END.equals(directive.name)) {
            processInvokeEnd(directive, lineNum);
        } else if (TAG_GET_FIELD.equals(directive.name)) {
            processGetField(directive, lineNum);
        } else if (TAG_CLASSNAME_FOR_METHOD.equals(directive.name)) {
            processClassNameForMethod(directive,lineNum);
        } else if (TAG_LINKER_DECLARATIONS.equals(directive.name)) {
            processLinkerDeclarations(directive, lineNum);
        } else if (TAG_LINKER_INITIALIZATIONS.equals(directive.name)) {
            processLinkerDefinitions(directive, lineNum);
        } else {
            throw err("Unrecoginized directive: %s", directive.name);
        }

    }
    
    private void processInvokeBegin(Directive directive, int lineNum) throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            finest("Processing invoke-begin directive: %s",directive);
        }
        MethodReference methRef = targets.getTargetMethod(lineNum);
        int includeStackSize = directive.getIntAttribute("includeStackSize", -1);
        if (includeStackSize < 0 || includeStackSize > 1)
            throw err("bad invoke-begin directive: "+directive);

        if (methRef.getInvocationType() != MethodReference.InvocationType.STATIC) {
            formatln("\taload_0");
            if (!methRef.isLocal()) {
                String refField = targets.getModuleInstanceField(methRef.getTargetBinaryName());
                formatln("\tgetfield %s/%s L%s;", classInfo.getBinaryName(), refField,
                        methRef.getTargetBinaryName());
            }
            if (includeStackSize == 1) {
                formatln("\tswap");
            }
        }
    }
    
    private void processInvokeEnd(Directive directive, int lineNum) throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            finest("Processing invoke-end directive: %s",directive);
        }
        MethodReference methRef = targets.getTargetMethod(lineNum);
        switch (methRef.getInvocationType()) {
        case STATIC:
            formatln("\tinvokestatic %s ", methRef.getSymbolicReference());
            break;
        case VIRTUAL:
            formatln("\tinvokevirtual %s ", methRef.getSymbolicReference());
            break;
        case SPECIAL:
            formatln("\tinvokespecial %s ", methRef.getSymbolicReference());
            break;
        case INTERFACE:
            formatln("\tinvokeinterface %s %d", methRef.getSymbolicReference(),
                    calculateInvokeInterfaceCount(methRef.getSymbolicReference()));
            break;
        }
    }
    
    private void processGetField(Directive directive, int lineNum) throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            finest("Processing get-field directive: %s",directive);
        }
        FieldReference fieldRef = targets.getTargetField(lineNum);
        if (fieldRef.getAccessType() == FieldReference.AccessType.STATIC) {
            formatln("\tgetstatic %s", fieldRef.getSymbolicReference());
        } else {
            formatln("\taload_0");
            if (!fieldRef.isLocal()) {
                String refField = targets.getModuleInstanceField(fieldRef.getTargetBinaryName());
                formatln("\tgetfield %s/%s L%s;", binaryName(classInfo.getBinaryName()), refField,
                        fieldRef.getTargetBinaryName());
            }
            formatln("\tgetfield %s", fieldRef.getSymbolicReference());
        }

    }

    private void processClassNameForMethod(Directive directive, int lineNum) throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            finest("Processing classname-for-method directive: %s",directive);
        }
        //Change to work with class constants rather than classname
        MethodReference methRef = targets.getTargetMethod(lineNum);
        formatln("\tldc \"%s\"", methRef.getTargetBinaryName());
    }

    
    private void processLinkerDeclarations(Directive directive, int lineNum) throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            finest("Processing linker declarations directive: %s",directive);
        }
        for(String module : targets.getReferencedModules()) {
            String field = targets.getModuleInstanceField(module);
            formatln(".field private %s L%s;", field, module);
        }
    }
    
    private void processLinkerDefinitions(Directive directive, int lineNum) throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            finest("Processing linker definitions directive: %s",directive);
        }
        final String contextName = binaryName(Context.class.getName());
        final String getInstMethod = "lljvm/runtime/Context/getModule(Ljava/lang/Class;)Ljava/lang/Object;";
        final int getInstCnt = calculateInvokeInterfaceCount(getInstMethod);
        //Assume - this is in local #0, context in local #1
        for(String module : targets.getReferencedModules()) {
            String field = targets.getModuleInstanceField(module);
            formatln("");
            formatln("\taload_0");
            formatln("\taload_1");
            if (!contextName.equals(module)) {
                formatln("\tldc %s", module);
                formatln("\tinvokeinterface %s %d ", getInstMethod, getInstCnt);
                formatln("\tcheckcast %s ", module);
            }
            formatln("\tputfield %s/%s L%s;", classInfo.getBinaryName(), field, module);
        }
    }
    
    /**
     * Given a symbolic reference to an interface method, returns the appropriate 'count' value to
     * be used in the corresponding {@code invokeinterface} method.
     * 
     * @param symRef
     *            the symbolic reference
     * @return the count value
     */
    private static int calculateInvokeInterfaceCount(String symRef) {
        int start = symRef.indexOf('(') + 1;
        int end = symRef.lastIndexOf(')');
        int count = 0;
        boolean arr = false;
        for (int i = start; i < end; i++) {
            int c = symRef.charAt(i);
            if (c == '[') {
                arr = true;
                continue;
            }
            if (c == 'L') {
                i = symRef.indexOf(';', i + 1);
                if (i < 0)
                    throw new IllegalArgumentException(symRef);
            }
            count += (!arr && (c == 'J' || c == 'D')) ? 2 : 1;
            arr = false;
        }
        return 1 + count;
    }
    
    private void writeln(String s) throws IOException {
        out.write(s);
        out.write(lineSep);
    }
    
    private void formatln(String format, Object... args) throws IOException {
        fout.format(format, args);
        out.write(lineSep);
    }

    private void finest(String msg, Object... args) {
        logger.finest(withLine(String.format(msg, args)));
    }

    private void fine(String msg, Object... args) {
        logger.fine(withLine(String.format(msg, args)));
    }
}
