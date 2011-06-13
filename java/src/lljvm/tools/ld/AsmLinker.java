/*
 * Copyright (c) 2009 David Roberts <d@vidr.cc> 
 * Copyright (c) 2011 Joshua Arnold (modifications to original source code)
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lljvm.runtime.Context;

/**
 * Class for linking assembly files.
 * 
 * @author Joshua Arnold
 * @author David Roberts (wrote the original AsmLinker that this class was based on)
 */
public class AsmLinker {
    private static final Logger logger = Logger.getLogger(AsmLinker.class.getName());

    private static final String TAG_INVOKE_BEGIN = "INVOKE-BEGIN";
    private static final String TAG_INVOKE_END = "INVOKE-END";
    private static final String TAG_GET_FIELD = "GET-FIELD";
    private static final String TAG_CLASSNAME_FOR_METHOD = "CLASSNAME-FOR-METHOD";
    private static final String TAG_LINKER_HEADER = "LINKER-HEADER";
    private static final String TAG_LINKER_DECLARATIONS = "LINKER-DECLARATIONS";
    private static final String TAG_LINKER_INITIALIZATIONS = "LINKER-INITIALIZATIONS";

    private static final String LINK_DECLARE_PLACEHOLDER = "LINK_DECLARE_PLACEHOLDER fa91680a-7043-4fc7-9d4b-147ed88c826a";
    private static final String LINK_DEFINE_PLACEHOLDER = "LINK_DEFINE_PLACEHOLDER a5ff7819-07fa-414a-b28b-08bc2c799981";

    /** The reader to read input assembly code */
    private final LineNumberReader in;
    /** The writer to write linked assembly code */
    private final PrintWriter out;
    /** The resolver to use */
    private final Resolver resolver;
    /** If non null, contains the current method invocation being generated */
    private InvocationSequence currentInvocation;
    /** The name of the class being generated. Initialized when the header tag is processed */
    private String className;
    /** Map between classes and their corresponding reference fields */
    private final Map<String, String> refFieldNames = new LinkedHashMap<String, String>();
    /**
     * True if we've warned about the current invocation seeming excessively long. Prevents further
     * warnings so we don't spam the output.
     */
    private boolean longInvocationWarning;

    /**
     * Construct a new AsmLinker with the specified input and output sources.
     * 
     * @param in
     *            the reader to read input assembly code
     * @param out
     *            the writer to write linked assembly code
     * @param resolver
     *            the resolver instance used to resolve symbolic references
     */
    public AsmLinker(LineNumberReader in, PrintWriter out, Resolver resolver) {
        this.in = in;
        this.out = out;
        this.resolver = resolver;
    }

    /**
     * Performs the link operation.
     * 
     * @throws IOException
     */
    public void link() throws IOException {
        // Currently we're doing this in two passes and using a temp file for intermediate results.
        File tmpFile = File.createTempFile("lljvm-", ".tmp");
        try {
            linkPass0(tmpFile);
            linkPass1(tmpFile);
        } finally {
            try {
                tmpFile.delete();
                if (tmpFile.exists()) {
                    logger.warning("Unable to delete temporary file: " + tmpFile.getAbsolutePath());
                }
            } finally {
                out.flush();
            }
        }
    }

    /**
     * Performs the first pass of the linker. Intermediate output is written to the specified temp
     * file.
     */
    private void linkPass0(File tmpFile) throws IOException {
        PrintWriter tmpOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tmpFile), "UTF-8")));
        try {
            for (String line; (line = in.readLine()) != null;) {
                Directive d = Directive.parse(line);
                Collection<String> result;
                if (d != null)
                    result = processDirective(d);
                else
                    result = Collections.singletonList(line);
                if (currentInvocation != null) {
                    // If there's an invocation in progress, record the result there.
                    if (!longInvocationWarning
                            && (currentInvocation.argumentInstructions.size() + result.size()) > 1000) {
                        logger.warning(String
                                .format("The current invocation sequence seems excesively long; starting line=%d, current line=%d",
                                        currentInvocation.startDirective, in.getLineNumber()));
                        longInvocationWarning = true;
                    }
                    currentInvocation.argumentInstructions.addAll(result);
                } else {
                    // Otherwise, write the result to the output.
                    for (String rl : result)
                        tmpOut.println(rl);
                }
            }
            if (currentInvocation != null) {
                throw err(
                        "Reached end of input while call sequence from line %d was still in progress",
                        currentInvocation.startLineNumber);
            }
            tmpOut.flush();
        } finally {
            tmpOut.close();
        }
    }

    /**
     * Performs the second pass of the linker. The intermediate result is read from the temp file
     * and the final result is written to the output.
     */
    private void linkPass1(File tmpFile) throws IOException {
        BufferedReader tmpIn = new BufferedReader(new InputStreamReader(
                new FileInputStream(tmpFile), "UTF-8"));
        try {
            for (String line; (line = tmpIn.readLine()) != null;) {
                if (LINK_DECLARE_PLACEHOLDER.equals(line)) {
                    for (String s : generateLinkerDeclarations1())
                        out.println(s);
                } else if (LINK_DEFINE_PLACEHOLDER.equals(line)) {
                    for (String s : generateLinkerInitializations1())
                        out.println(s);
                } else {
                    out.println(line);
                }
            }
        } finally {
            tmpIn.close();
        }
    }

    /**
     * Processes a directive and returns 1 or more lines of linker output.
     */
    private Collection<String> processDirective(Directive directive) {
        if (TAG_LINKER_HEADER.equals(directive.name))
            return processHeader(directive);
        if (className == null)
            throw err("First directive was not a header directive: %s", directive);
        if (TAG_INVOKE_BEGIN.equals(directive.name)) {
            currentInvocation = new InvocationSequence(directive, in.getLineNumber(),
                    currentInvocation);
            return Collections.emptyList();
        }
        if (TAG_INVOKE_END.equals(directive.name)) {
            if (currentInvocation == null)
                throw err("Unbalanced end call directive");
            Collection<String> result = generateCall(currentInvocation, directive);
            currentInvocation = currentInvocation.parentSequence;
            if (currentInvocation == null)
                longInvocationWarning = false;
            return result;
        }
        if (TAG_GET_FIELD.equals(directive.name))
            return generateGetField(directive);
        if (TAG_LINKER_INITIALIZATIONS.equals(directive.name))
            return generateLinkerInitializations0(directive);
        if (TAG_LINKER_DECLARATIONS.equals(directive.name))
            return generateLinkerDeclarations0(directive);
        if (TAG_CLASSNAME_FOR_METHOD.equals(directive.name))
            return generateClassNameForMethod(directive);

        throw err("Unrecoginized directive: %s", directive.name);
    }

    /**
     * Returns the field name containing a reference to the specified class module.
     */
    private String referenceFieldForClass(String className) {
        className = binaryName(className);
        if (refFieldNames.containsKey(className))
            return refFieldNames.get(className);
        Set<String> used = new HashSet<String>(refFieldNames.values());
        String[] parts = className.split("\\/");
        String base = parts[parts.length - 1].toLowerCase(Locale.US);
        String ref;
        for (int i = 0;; i++) {
            ref = "__link$" + base + "$" + i;
            if (!used.contains(ref))
                break;
        }
        refFieldNames.put(className, ref);
        return ref;
    }

    /**
     * Generates a method invocation sequence.
     * 
     * @param sequence
     *            describes the sequence
     * @param endDirective
     *            the directive terminating the sequence
     * @return the linker output for the sequence
     */
    private Collection<String> generateCall(InvocationSequence sequence, Directive endDirective) {
        String sig = endDirective.getStringAttribute("sig");
        int includeStackSize = sequence.startDirective.getIntAttribute("includeStackSize", -1);
        if (sig.isEmpty() || includeStackSize < 0 || includeStackSize > 1)
            throw err("bad call directives: start=%s, end=%s", sequence.startDirective,
                    endDirective);
        boolean local = endDirective.getBooleanAttribute("local");

        MethodReference methRef;
        try {
            methRef = local ? resolver.resolveLocalMethod(sig, className) : resolver
                    .resolveMethod(sig);
        } catch (AsmLinkerException e) {
            throw withLine(e);
        }

        ArrayList<String> res = new ArrayList<String>();
        if (methRef.getInvocationType() != MethodReference.InvocationType.STATIC) {
            formatTo(res, "\taload_0");
            if (!methRef.isLocal()) {
                String refField = referenceFieldForClass(methRef.getTargetBinaryName());
                formatTo(res, "\tgetfield %s/%s L%s;", binaryName(className), refField,
                        methRef.getTargetBinaryName());
            }
            if (includeStackSize == 1) {
                formatTo(res, "\tswap");
            }
        }
        res.addAll(sequence.argumentInstructions);
        switch (methRef.getInvocationType()) {
        case STATIC:
            formatTo(res, "\tinvokestatic %s ", methRef.getSymbolicReference());
            break;
        case VIRTUAL:
            formatTo(res, "\tinvokevirtual %s ", methRef.getSymbolicReference());
            break;
        case SPECIAL:
            formatTo(res, "\tinvokespecial %s ", methRef.getSymbolicReference());
            break;
        case INTERFACE:
            formatTo(res, "\tinvokeinterface %s %d", methRef.getSymbolicReference(),
                    calculateInvokeInterfaceCount(methRef.getSymbolicReference()));
            break;
        }
        return res;
    }

    /**
     * Process the {@link #TAG_GET_FIELD} tag.
     */
    private Collection<String> generateGetField(Directive directive) {
        String sig = directive.getStringAttribute("sig");
        if (sig.isEmpty())
            throw err("Invalid get-field directive %s", directive);
        boolean local = directive.getBooleanAttribute("local");

        FieldReference fieldRef;
        try {
            fieldRef = local ? resolver.resolveLocalField(sig, className) : resolver
                    .resolveField(sig);
        } catch (AsmLinkerException e) {
            throw withLine(e);
        }

        ArrayList<String> res = new ArrayList<String>();
        if (fieldRef.getAccessType() == FieldReference.AccessType.STATIC) {
            formatTo(res, "\tgetstatic %s", fieldRef.getSymbolicReference());
        } else {
            formatTo(res, "\taload_0");
            if (!fieldRef.isLocal()) {
                String refField = referenceFieldForClass(fieldRef.getTargetBinaryName());
                formatTo(res, "\tgetfield %s/%s L%s;", binaryName(className), refField,
                        fieldRef.getTargetBinaryName());
            }
            formatTo(res, "\tgetfield %s", fieldRef.getSymbolicReference());
        }
        return res;
    }

    /**
     * Process the {@link #TAG_CLASSNAME_FOR_METHOD} tag.
     */
    private Collection<String> generateClassNameForMethod(Directive directive) {
        String sig = directive.getStringAttribute("sig");
        if (sig.isEmpty())
            throw err("bad directive %s", directive);

        MethodReference methRef;
        try {
            methRef = resolver.resolveMethod(sig);
        } catch (AsmLinkerException e) {
            throw withLine(e);
        }
        String inst = String.format("\tldc \"%s\"", methRef.getTargetBinaryName());
        return Collections.singletonList(inst);
    }

    /**
     * Processes the linker initializations tag.
     */
    private Collection<String> generateLinkerInitializations0(Directive directive) {
        // Just write a placeholder. We'll do the actual work on the next pass.
        return Collections.singletonList(LINK_DEFINE_PLACEHOLDER);
    }

    /**
     * Generates the linker initialization code.
     */
    private Collection<String> generateLinkerInitializations1() {
        List<String> res = new ArrayList<String>();
        for (Map.Entry<String, String> entry : refFieldNames.entrySet()) {
            res.add("");
            res.add(String.format("\taload_0"));
            res.add(String.format("\taload_1"));
            if (!Context.class.getName().equals(javaName(entry.getKey()))) {
                res.add(String.format("\tldc %s", binaryName(entry.getKey())));
                String getInst = "lljvm/runtime/Context/getModule(Ljava/lang/Class;)Ljava/lang/Object;";
                res.add(String.format("\tinvokeinterface %s %d ", getInst,
                        calculateInvokeInterfaceCount(getInst)));
                res.add(String.format("\tcheckcast %s ", binaryName(entry.getKey())));
            }
            res.add(String.format("\tputfield %s/%s L%s;", binaryName(className), entry.getValue(),
                    binaryName(entry.getKey())));
        }
        return res;
    }

    /**
     * Processes the linker declaration tag.
     */
    private Collection<String> generateLinkerDeclarations0(Directive directive) {
        // Just write a placeholder. We'll do the actual work on the next pass.
        return Collections.singletonList(LINK_DECLARE_PLACEHOLDER);
    }

    /**
     * Generates the linker declarations.
     */
    private Collection<String> generateLinkerDeclarations1() {
        List<String> res = new ArrayList<String>();
        for (Map.Entry<String, String> entry : refFieldNames.entrySet()) {
            res.add(String.format(".field private final %s L%s;", entry.getValue(), entry.getKey()));
        }
        return res;
    }

    /**
     * Processes the linker header tag.
     */
    private Collection<String> processHeader(Directive directive) {
        if (className != null)
            throw err("Duplicate header directive");
        if (directive.getStringAttribute("class").isEmpty())
            throw err("Header directive is missing class attribute");
        className = directive.getStringAttribute("class");

        // Ensure there we hold a reference to our context
        referenceFieldForClass(Context.class.getName());

        return Collections.emptyList();

    }

    /**
     * Describes a linker directive found from the input file.
     */
    private static class Directive {
        private static final Pattern format = Pattern
                .compile("\\s*\\*\\*LLJVM\\-([^\\*]+)\\*\\*\\s*(?:\\|(.*))?");
        final String name;
        final Map<String, String> attributes;

        Directive(String name, Map<String, String> attributes) {
            super();
            this.name = name;
            this.attributes = attributes;
        }

        static Directive parse(String line) {
            Matcher m = format.matcher(line);
            if (!m.matches())
                return null;
            String name = m.group(1);
            String attrs = m.group(2);
            if (attrs == null)
                return new Directive(name, Collections.<String, String> emptyMap());

            Map<String, String> amap = new HashMap<String, String>();
            for (String attr : attrs.split("\\|")) {
                attr = attr.trim();
                if (attr.length() == 0)
                    continue;
                String[] nv = attr.split("\\=", 2);
                amap.put(nv[0].trim(), nv.length > 1 ? nv[1].trim() : "");
            }
            return new Directive(name, Collections.unmodifiableMap(amap));
        }

        @Override
        public String toString() {
            return name + attributes;
        }

        String getStringAttribute(String name) {
            return attributes.containsKey(name) ? attributes.get(name) : "";
        }

        boolean getBooleanAttribute(String name) {
            return Boolean.valueOf(attributes.get(name));
        }

        int getIntAttribute(String name, int deflt) {
            if (attributes.containsKey(name)) {
                String s = attributes.get(name);
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return deflt;
                }
            }
            return deflt;
        }

    }

    /**
     * Contains information about the current method invocation being generated.
     */
    private static class InvocationSequence {
        /**
         * The directive that started the invocation.
         */
        final Directive startDirective;
        /**
         * The parent invocation if this is a nested invocation, <code>null</code> otherwise.
         */
        final InvocationSequence parentSequence;
        /**
         * The line number of the start directive.
         */
        final int startLineNumber;
        /**
         * Holds the linker output for instructions contained within this sequence.
         */
        final List<String> argumentInstructions = new LinkedList<String>();

        InvocationSequence(Directive startDirective, int startLineNumber,
                InvocationSequence parentSequence) {
            super();
            this.startDirective = startDirective;
            this.startLineNumber = startLineNumber;
            this.parentSequence = parentSequence;
        }

    }

    private static void formatTo(Collection<? super String> dest, String format, Object... args) {
        dest.add(String.format(format, args));
    }

    /**
     * Creates an AsmLinkerException with a formatted string message.
     * <p>
     * The current line number is automatically appended to the message.
     * </p>
     */
    private AsmLinkerException err(String format, Object... args) {
        String msg = String.format(format, args) + "; currentLine=" + in.getLineNumber();
        return new AsmLinkerException(msg);
    }

    /**
     * Replaces an {@link AsmLinkerException} with a new exception containing line number
     * information but is otherwise identical.
     */
    private AsmLinkerException withLine(AsmLinkerException src) {
        String msg = src.getMessage() + "; currentLine=" + in.getLineNumber();
        AsmLinkerException res = new AsmLinkerException(msg, src.getCause());
        res.setStackTrace(src.getStackTrace());
        return res;
    }

    private static String binaryName(String s) {
        return s.replace('.', '/');
    }

    private static String javaName(String s) {
        return s.replace('/', '.');
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
}
