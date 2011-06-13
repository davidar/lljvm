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
 * Describes a resolved field reference.
 * 
 * @author Joshua Arnold
 */
public final class FieldReference {

    public enum AccessType {
        /** References a static field */
        STATIC,
        /** References an instance field */
        INSTANCE
    };

    public enum Flags {
        /** References a field on the referring instance */
        LOCAL
    }

    private final String symbolicReference;
    private final AccessType accessType;
    private final EnumSet<Flags> flags;

    /**
     * Constructor.
     * 
     * @param symbolicReference
     *            the full symbolic reference to the field
     * @param accessType
     *            the type of access instruction to use to access the field
     * @param flags
     *            boolean flags associated with the reference
     */
    public FieldReference(String symbolicReference, AccessType accessType, Set<Flags> flags) {
        super();
        this.symbolicReference = symbolicReference;
        this.accessType = accessType;
        this.flags = flags != null && !flags.isEmpty() ? EnumSet.copyOf(flags) : EnumSet
                .noneOf(Flags.class);
    }

    /**
     * Returns the full symbolic reference to the field
     * 
     * @return the full symbolic reference to the field
     */
    public String getSymbolicReference() {
        return symbolicReference;
    }

    /**
     * Returns the target binary class name of the field.
     * 
     * @return the target binary class name of the field.
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
     * Returns the type of field.
     * 
     * @return the type of field.
     */
    public AccessType getAccessType() {
        return accessType;
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
}
