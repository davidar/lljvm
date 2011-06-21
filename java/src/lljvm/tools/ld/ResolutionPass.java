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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lljvm.runtime.Context;
import lljvm.util.ClassInfo;

/**
 * A {@link LinkerPass} that resolves method and field references generates a {@link ResolvedTargets} for use by a {@link WriterPass}.
 * @author Joshua Arnold
 *
 */
public class ResolutionPass extends LinkerPass<ResolvedTargets> {
    private static final Logger logger = Logger.getLogger(ResolutionPass.class.getName());

    private LineTable<Object> references;

    private Map<String, String> moduleFieldNamesByClassName;

    private int methodsResolved;

    private int classNamesResolved;

    private int fieldsResolved;

    private Deque<Integer> startCallLines;

    private final Resolver resolver;

    private final ClassInfo classInfo;

    ResolutionPass(AsmSource file, ClassInfo classInfo, Resolver resolver) {
        super(file);
        this.resolver = resolver;
        this.classInfo = classInfo;
    }

    @Override
    protected void init() throws IOException {
        methodsResolved = 0;
        classNamesResolved = 0;
        fieldsResolved = 0;
        references = new LineTable<Object>();
        moduleFieldNamesByClassName = new LinkedHashMap<String, String>();
        startCallLines = new ArrayDeque<Integer>();
        
        // Ensure reference to the context
        moduleInstanceReferenced(binaryName(Context.class.getName()));

        if (logger.isLoggable(Level.FINE))
            fine("Start Resolution Pass");
    }

    @Override
    protected ResolvedTargets complete() throws IOException {
        if (!startCallLines.isEmpty())
            throw err("Unbalanced invoke-start tag");

        ResolvedTargets targets = new Targets(references, moduleFieldNamesByClassName);

        if (logger.isLoggable(Level.FINE)) {
            fine("%d method references were resolved", methodsResolved);
            fine("%d field references were resolved", fieldsResolved);
            fine("%d classname constants were resolved", classNamesResolved);
            fine("%d distinct modules were referenced", moduleFieldNamesByClassName.size());
            fine("End Resolution Pass");
        }

        return targets;
    }

    @Override
    protected void cleanup() {
        references = null;
        moduleFieldNamesByClassName = null;
        startCallLines = null;
    }

    @Override
    protected void processLine(String line, int lineNum) throws IOException {
        Directive linkerDirective = Directive.parse(line);
        if (linkerDirective == null)
            return;
        if (TAG_INVOKE_BEGIN.equals(linkerDirective.name)) {
            processInvokeBeginTag(linkerDirective, lineNum);
        } else if (TAG_INVOKE_END.equals(linkerDirective.name)) {
            processInvokeEndTag(linkerDirective, lineNum);
        } else if (TAG_GET_FIELD.equals(linkerDirective.name)) {
            processGetFieldTag(linkerDirective, lineNum);
        } else if (TAG_CLASSNAME_FOR_METHOD.equals(linkerDirective.name)) {
            processClassnameForMethodTag(linkerDirective, lineNum);
        }
    }

    void processInvokeBeginTag(Directive directive, int lineNum) {
        startCallLines.push(lineNum);
        references.add(lineNum, null);
    }

    void processInvokeEndTag(Directive directive, int lineNum) {
        if (startCallLines.isEmpty())
            throw err("Unbalanced invoke-end tag");
        final int invokeBeginLine = startCallLines.pop();

        String sig = directive.getStringAttribute("sig");
        if (sig.isEmpty())
            throw err("Invalid invoke-end directive");
        boolean local = directive.getBooleanAttribute("local");

        MethodReference ref;
        if (local)
            ref = resolver.resolveLocalMethod(sig, classInfo);
        else
            ref = resolver.resolveMethod(sig, classInfo);

        references.change(invokeBeginLine, ref);
        references.add(lineNum, ref);
        if (!ref.isLocal() && ref.getInvocationType()!=MethodReference.InvocationType.STATIC)
            moduleInstanceReferenced(ref.getTargetBinaryName());
        methodsResolved++;

        if (logger.isLoggable(Level.FINEST)) {
            finest("Resolved method %s (%d to %d)", ref.getSymbolicReference(), invokeBeginLine,
                    lineNum);
        }
    }

    void processGetFieldTag(Directive directive, int lineNum) {
        String sig = directive.getStringAttribute("sig");
        if (sig.isEmpty())
            throw err("Invalid get-field directive %s", directive);
        boolean local = directive.getBooleanAttribute("local");
        FieldReference ref;
        if (local)
            ref = resolver.resolveLocalField(sig, classInfo);
        else
            ref = resolver.resolveField(sig, classInfo);

        references.add(lineNum, ref);
        if (!ref.isLocal() && ref.getAccessType()!=FieldReference.AccessType.STATIC)
            moduleInstanceReferenced(ref.getTargetBinaryName());
        fieldsResolved++;

        if (logger.isLoggable(Level.FINEST)) {
            finest("Resolved field %s", ref.getSymbolicReference());
        }
    }

    void processClassnameForMethodTag(Directive directive, int lineNum) {
        String sig = directive.getStringAttribute("sig");
        if (sig.isEmpty())
            throw err("bad directive %s", directive);
        MethodReference ref = resolver.resolveMethod(sig, classInfo);

        references.add(lineNum, ref);
        classNamesResolved++;

        if (logger.isLoggable(Level.FINEST)) {
            finest("Resolved classname constant for method %s", ref.getSymbolicReference());
        }
    }

    private void moduleInstanceReferenced(String binaryClassName) {
        if (moduleFieldNamesByClassName.containsKey(binaryClassName))
            return;

        int p = binaryClassName.lastIndexOf('/');
        String simpleName = (p >= 0 ? binaryClassName.substring(p + 1) : binaryClassName)
                .toLowerCase(Locale.US);
        Set<String> usedNames = new HashSet<String>(moduleFieldNamesByClassName.values());

        for (int i = 0;; i++) {
            String fieldName = "__link$ref$" + simpleName + "$" + i;
            if (!usedNames.contains(fieldName)) {
                moduleFieldNamesByClassName.put(binaryClassName, fieldName);
                break;
            }
        }
    }

    private static class Targets implements ResolvedTargets {
        private final LineTable<Object> references;
        private final Map<String, String> moduleFieldNamesByClassName;

        Targets(LineTable<Object> references, Map<String, String> moduleFieldNamesByClassName) {
            super();
            this.references = references;
            this.moduleFieldNamesByClassName = moduleFieldNamesByClassName;
        }

        @Override
        public MethodReference getTargetMethod(int lineNum) {
            Object obj = references.get(lineNum);
            if (!(obj instanceof MethodReference))
                throw new AsmLinkerException("No method ref at " + lineNum);
            return (MethodReference) obj;
        }

        @Override
        public FieldReference getTargetField(int lineNum) {
            Object obj = references.get(lineNum);
            if (!(obj instanceof FieldReference))
                throw new AsmLinkerException("No method ref at " + lineNum);
            return (FieldReference) obj;
        }

        @Override
        public String getModuleInstanceField(String moduleClassBinaryName) {
            String fieldName = moduleFieldNamesByClassName.get(moduleClassBinaryName);
            if (fieldName == null)
                throw new AsmLinkerException("No field for " + moduleClassBinaryName);
            return fieldName;
        }

        @Override
        public Collection<String> getReferencedModules() {
            return Collections.unmodifiableSet(moduleFieldNamesByClassName.keySet());
        }

    }

    private void finest(String msg, Object... args) {
        logger.finest(withLine(String.format(msg, args)));
    }

    private void fine(String msg, Object... args) {
        logger.fine(withLine(String.format(msg, args)));
    }

}
