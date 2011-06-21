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
package lljvm.util;

import java.lang.reflect.Modifier;

/**
 * Describes a method or a field.
 * @author Joshua Arnold
 */
public abstract class MemberInfo {
    protected final String qualifiedSignature;
    protected final int modifiers;
    MemberInfo(String qualifiedSignature, int modifiers) {
        super();
        this.qualifiedSignature = qualifiedSignature;
        this.modifiers = modifiers;
        if (qualifiedSignature==null || qualifiedSignature.isEmpty() || qualifiedSignature.indexOf('.')>=0)
            throw new IllegalArgumentException(qualifiedSignature);
    }
    
    public final String getQualifiedSignature() {
        return qualifiedSignature;
    }
    
    public abstract String getSignature();

    public abstract String getBinaryClassName();

    public abstract String getMemberName();

    public abstract String getType();
    

    public final int getModifiers() {
        return modifiers;
    }

    public final boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    public final boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    public final boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    public final boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public final boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    final boolean sigAndModsEquals(MemberInfo info) {
        return qualifiedSignature.equals(info.qualifiedSignature) && modifiers == info.modifiers;
    }
    
    final int sigAndModsHashcode() {
        return qualifiedSignature.hashCode() + 127 * modifiers;
    }
}
