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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class LinkerPass<T> implements Callable<T> {

    private static final Logger logger = Logger.getLogger(LinkerPass.class.getName());

    protected static final String TAG_INVOKE_BEGIN = "INVOKE-BEGIN";
    protected static final String TAG_INVOKE_END = "INVOKE-END";
    protected static final String TAG_GET_FIELD = "GET-FIELD";
    protected static final String TAG_CLASSNAME_FOR_METHOD = "CLASSNAME-FOR-METHOD";
    protected static final String TAG_LINKER_HEADER = "LINKER-HEADER";
    protected static final String TAG_LINKER_DECLARATIONS = "LINKER-DECLARATIONS";
    protected static final String TAG_LINKER_INITIALIZATIONS = "LINKER-INITIALIZATIONS";

    protected final AsmSource file;

    private LineNumberReader in;
    
    private static final int BUFSZ = 1<<15;

    LinkerPass(AsmSource file) {
        super();
        this.file = file;
    }

    @Override
    public final T call() {
        try {
            in = readerFor(file.startInput());
            try {
                init();
                for (String line; (line = in.readLine()) != null;) {
                    processLine(line, in.getLineNumber());
                }
                return complete();
            } finally {
                cleanup();
            }
        } catch (AsmLinkerException e) {
            throw fillIn(e);
        } catch (IOException e) {
            throw fillIn(new AsmLinkerException("IO failure: " + e, e));
        } catch (Throwable t) {
            // Log an entry so we get the source file and line number. Don't attach the throwable
            // itself to the log since it will get reported further up the stack.
            logger.log(Level.SEVERE, withLine("Unexpected exception [" + t + "]"));
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            if (t instanceof Error)
                throw (Error) t;
            throw new RuntimeException(t);
        } finally {
            try {
                if (in!=null)
                    in.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing " + file.getName(), e);
            } finally {
                in = null;
            }
        }
    }

    private AsmLinkerException fillIn(AsmLinkerException e) {
        if (e.getFileName() == null)
            e.setFileName(file.getName());
        if (e.getLineNum() < 0 && in!=null)
            e.setLineNum(in.getLineNumber());
        return e;
    }

    abstract protected void init() throws IOException;;

    abstract protected void processLine(String line, int lineNum) throws IOException;

    abstract protected T complete() throws IOException;

    abstract protected void cleanup() throws Exception;

    protected static LineNumberReader readerFor(InputStream is) {
        try {
            return new LineNumberReader(new InputStreamReader(new BufferedInputStream(is,BUFSZ), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e); // UTF-8 is guaranteed
        }
    }

    /**
     * Describes a linker directive found from the input file.
     */
    protected static final class Directive {
        
        final String name;
        final Map<String, String> attributes;

        public Directive(String name, Map<String, String> attributes) {
            super();
            this.name = name;
            this.attributes = attributes;
        }

        public static Directive parse(String line) {
            /*
             * Using reg-exps would be easier, but I've found that getting rid of them improves
             * the run time of the linker.  This is partly because the linker doesn't usually
             * usually run long enough for the JIT to do much optimization. 
             */
            final int len = line.length();
            char c = 0;
            int start;
            for(start=0;start<len;start++) {
                c = line.charAt(start);
                if (c>' ')
                    break;
            }
            if (c!='*')
                return null;
            if (!line.regionMatches(start+1, "LLJVM|", 0, 6))
                return null;
            final int nameStart = start+7;
            int nameEnd = line.indexOf('|',nameStart);
            if (nameEnd<0)
                return new Directive(line.substring(nameStart), Collections.<String,String>emptyMap());
                
            final String name = line.substring(nameStart,nameEnd);
            final String attrs = line.substring(nameEnd+1).trim();
            
            Map<String, String> amap = parseAttrs(attrs);
            return new Directive(name, amap);
        }

        private static Map<String, String> parseAttrs(String attrs) {
            Map<String, String> amap = new HashMap<String, String>(4,0.76f);
            final int alen = attrs.length();
            int obp = 0;
            for (int nobp; obp < alen; obp = nobp + 1) {
                nobp = attrs.indexOf('|', obp);
                if (nobp < 0)
                    nobp = alen;
                String attr = attrs.substring(obp, nobp);
                if (attr.isEmpty())
                    continue;
                int eqp = attr.indexOf('=');
                if (eqp < 0) {
                    amap.put(attr, "");
                } else {
                    amap.put(attr.substring(0,eqp),attr.substring(eqp+1));
                }
            }
            return Collections.unmodifiableMap(amap);
        }

        @Override
        public String toString() {
            return name + attributes;
        }

        public String getStringAttribute(String name) {
            return attributes.containsKey(name) ? attributes.get(name) : "";
        }

        public boolean getBooleanAttribute(String name) {
            return Boolean.valueOf(attributes.get(name));
        }

        public int getIntAttribute(String name, int deflt) {
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
     * Creates an AsmLinkerException with a formatted string message.
     */
    protected final AsmLinkerException err(String format, Object... args) {
        String msg = String.format(format, args);
        return new AsmLinkerException(msg);
    }

    protected final String withLine(String msg) {
        if (in != null)
            return msg + " : " + file.getName() + "(" + in.getLineNumber() + ")";
        else
            return msg + " : " + file.getName();
    }

    protected static String binaryName(String s) {
        return s.replace('.', '/');
    }

    protected static String javaName(String s) {
        return s.replace('/', '.');
    }

}
