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

import java.util.EnumSet;
import java.util.Set;

/**
 * Describes a resolved method reference.
 * 
 * @author Joshua Arnold
 */
public final class MethodReference {

    public enum InvocationType {
        /** Indicates {@code invokevirtual} is used to call this method. */
        VIRTUAL,
        /** Indicates {@code invokeinterface} is used to call this method. */
        INTERFACE,
        /** Indicates {@code invokespecial} is used to call this method. */
        SPECIAL,
        /** Indicates {@code invokestatic} is used to call this method. */
        STATIC
    };

    public enum Flags {
        /** Indicates that a method is being called on the referencing instance. */
        LOCAL
    }

    private final String symbolicReference;
    private final InvocationType invocationType;
    private final EnumSet<Flags> flags;

    private int hash;
    /**
     * Constructor.
     * 
     * @param symbolicReference
     *            the full symbolic reference to the method
     * @param invocationType
     *            the type of invocation instruction to use to call the method
     * @param flags
     *            boolean flags associated with the reference
     */
    public MethodReference(String symbolicReference, InvocationType invocationType, Set<Flags> flags) {
        super();
        this.symbolicReference = symbolicReference;
        this.invocationType = invocationType;
        this.flags = flags != null && !flags.isEmpty() ? EnumSet.copyOf(flags) : EnumSet
                .noneOf(Flags.class);
    }

    /**
     * Returns the full symbolic reference to the method
     * 
     * @return the full symbolic reference to the method
     */
    public String getSymbolicReference() {
        return symbolicReference;
    }

    /**
     * Returns the target binary class name of the method.
     * 
     * @return the target binary class name of the method.
     */
    public String getTargetBinaryName() {
        String res = symbolicReference;
        int p = res.indexOf('(');
        if (p >= 0)
            res = res.substring(0, p);
        p = res.lastIndexOf('/');
        return p >= 0 ? res.substring(0, p) : res;
    }

    /**
     * Returns a set of boolean flags associated with the reference.
     * 
     * @return a set of boolean flags associated with the reference.
     */
    public Set<Flags> getFlags() {
        return flags.clone();
    }

    /**
     * Returns the invocation type of the method.
     * 
     * @return the invocation type of the method.
     */
    public InvocationType getInvocationType() {
        return invocationType;
    }

    /**
     * Returns <code>true</code> if and only if the reference contains the {@link Flags#LOCAL LOCAL}
     * flag.
     * 
     * @return <code>true</code> if and only if the reference contains the {@link Flags#LOCAL LOCAL}
     *         flag.
     * @see #getFlags()
     */
    public boolean isLocal() {
        return flags.contains(Flags.LOCAL);
    }

    private static final int HASH_PRIME_1 = 80489;
    
    @Override
    public int hashCode() {
        int h = hash;
        if (h==0) {
            h = symbolicReference.hashCode() + HASH_PRIME_1 * (invocationType.hashCode() + HASH_PRIME_1 * flags.hashCode());
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==this)
            return true;
        if (obj==null)
            return false;
        if (!(obj instanceof MethodReference))
            return false;
        MethodReference mref = (MethodReference)obj;
        return symbolicReference.equals(mref.symbolicReference)
            && invocationType == mref.invocationType
            && flags.equals(mref.flags);
    }

    @Override
    public String toString() {
        return symbolicReference+" "+invocationType+" "+flags;
    }
    
    

}
