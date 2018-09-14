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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract implementation of {@link AsmSource} that enforces some of the latter interface's synchronization requirements.
 * @author Joshua Arnold
 */
public abstract class AbstractAsmSource implements AsmSource {
    
    private static final int OUTPUT_NOT_CREATED = 0;
    private static final int OUTPUT_OPEN = 1;
    private static final int OUTPUT_CLOSED = 2;
    
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Semaphore inPermit = new Semaphore(1);
    private final AtomicInteger outputState = new AtomicInteger(OUTPUT_NOT_CREATED);
    
    @Override
    public final InputStream startInput() throws IOException {
        if (closed.get())
            throw new IllegalStateException("closed");
        if (!inPermit.tryAcquire())
            throw new IllegalStateException("Input stream already active");
        In in = null;
        try {
            return in = new In(createInputStream(),inPermit);
        } finally {
            if (in==null)
                inPermit.release();
        }
    }
    
    /**
     * Implementations should create and return their input stream(s) from this method.
     * The {@code AbstractAsmSource} guarantees that this method will not be called until
     * the prior input stream (if any) has been closed. 
     * @return the stream
     * @throws IOException
     */
    protected abstract InputStream createInputStream() throws IOException;

    @Override
    public final OutputStream startOutput() throws IOException {
        if (closed.get())
            throw new IllegalStateException("closed");
        if (!outputState.compareAndSet(OUTPUT_NOT_CREATED,OUTPUT_OPEN))
            throw new IllegalStateException("Output stream was already created");
        Out out = null;
        try {
            return out = new Out(createOutputStream(),outputState);
        } finally {
            if (out==null)
                outputState.set(OUTPUT_CLOSED);
        }
    }
    
    /**
     * Implementations should create and return their output stream.
     * The {@code AbstractAsmSource} guarantees that this method will only be called once.
     * @return the stream
     * @throws IOException
     */
    protected abstract OutputStream createOutputStream() throws IOException;
    
    
    public final void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            try {
                if (!outputState.compareAndSet(OUTPUT_NOT_CREATED, OUTPUT_CLOSED)) {
                    if (outputState.get()!=OUTPUT_CLOSED)
                        throw new IllegalStateException("Output stream still active");
                }
                if (!inPermit.tryAcquire())
                    throw new IllegalStateException("Input stream still active");
                complete();
            } finally {
                cleanup();
            }           
        }
    }
    
    /**
     * Called when this {@link AsmSource} has been {@linkplain AsmSource#close() closed}.
     * <p>
     * Implementations should perform any actions that need to occur to put the source in its
     * final state.  For example, if an implementation wrote to a temporary file, it could copy
     * the temporary file to the final file from here.
     * </p> 
     * <p>
     * Note that this method will only be called if all the active input and output streams associated 
     * with the source have been closed.  If this condition is violated, the {@link #close()} will throw
     * an exception and this method will not be called.  
     * </p>
     * @throws IOException
     * @see #cleanup()
     */
    protected abstract void complete() throws IOException;
    
    /**
     * Unconditionally called when this {@link AsmSource} has been {@linkplain AsmSource#close() closed}.
     * <p>
     * This method is normally called after {@link #complete()} is called.  It differs from the latter in that
     * it is guaranteed to be called even in the event of an error but that it does not guarantee that its
     * the source's streams have been closed.   It is intended for non-critical cleanup work.
     * </p> 
     */
    protected abstract void cleanup();
    
    
    
    private static final class In extends FilterInputStream {
        private final Semaphore permit;
        In(InputStream s, Semaphore permit) {
            super(s);
            if (s==null)
                throw new IllegalArgumentException();
            this.permit = permit;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                permit.release();
            }
        }
    }
    
    private static final class Out extends OutputStream {
        private final AtomicInteger outputState;
        private final OutputStream out;
        Out(OutputStream s, AtomicInteger outputState) {
            super();
            if (s==null)
                throw new IllegalArgumentException();
            this.out = s;
            this.outputState = outputState;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                outputState.set(OUTPUT_CLOSED);
            }
        }
    }
}
