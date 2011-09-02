package lljvm.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lljvm.runtime.Context;
import lljvm.runtime.Module;

public class StreamStandardHandleFactory implements StandardHandleFactory, Module {
    private Context context;
    private final InputStream stdin;
    private final OutputStream stdout;
    private final OutputStream stderr;
    
    public StreamStandardHandleFactory(InputStream stdin, OutputStream stdout, OutputStream stderr) {
        super();
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
    }

    @Override
    public void destroy(Context context) {
    }

    @Override
    public FileHandle createStdin() {
        return new InputStreamFileHandle(context, stdin);
    }

    @Override
    public FileHandle createStdout() {
        return new OutputStreamFileHandle(context, stdout);
    }

    @Override
    public FileHandle createStderr() {
        return new OutputStreamFileHandle(context, stderr);
    }
    
    /**
     * Decorates an input stream such that {@link InputStream#close()} has no effect.
     * @param stream the base stream
     * @return the decorated stream
     */
    public static InputStream noClose(InputStream stream) {
        return new NoCloseInputStream(stream);
    }
    /**
     * Decorates an output stream such that {@link OutputStream#close()} is equivalent to {@link OutputStream#flush()}.
     * @param stream the base stream
     * @return the decorated stream
     */
    public static OutputStream noClose(OutputStream stream) {
        return new NoCloseOutputStream(stream);
    }
    
    private static final class NoCloseOutputStream extends OutputStream {
        private final OutputStream out;

        public NoCloseOutputStream(OutputStream out) {
            super();
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() throws IOException {
            out.flush();
        }        
    }
    
    private static final class NoCloseInputStream extends InputStream {
        private final InputStream in;

        NoCloseInputStream(InputStream in) {
            super();
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return in.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return in.read(b,off,len);
        }

        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() {}

        @Override
        public synchronized void mark(int readlimit) {
            in.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }
        
    }
    
}
