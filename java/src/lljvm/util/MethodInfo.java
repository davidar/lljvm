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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static lljvm.util.ReflectionUtils.modifiersFrom;

/**
 * Contains basic information about a method.
 * <p>
 * The primary advantage of {@code MethodInfo} over {@link Method} is that the former doesn't
 * require the method's class to be loaded.   For example, the ASM linker uses {@code MethodInfo}
 * to describe methods defined in an ASM source file.
 * </p> 
 * @author Joshua Arnold
 */
public final class MethodInfo extends MemberInfo {
    
    private final int methodDelim;
    private final int typeDelim;

    public MethodInfo(String qualifiedSignature, int modifiers) {
        super(qualifiedSignature,modifiers);
        typeDelim = qualifiedSignature.indexOf('(');
        if (typeDelim < 0)
            throw new IllegalArgumentException(qualifiedSignature);

        methodDelim = qualifiedSignature.lastIndexOf('/', typeDelim-1);
        if (methodDelim < 0)
            throw new IllegalArgumentException(qualifiedSignature);
    }
    
    public MethodInfo(String qualifiedSignature, Iterable<String> modifiers) {
        this(qualifiedSignature,modifiersFrom(modifiers));
    }
    public MethodInfo(String qualifiedSignature, String... modifiers) {
        this(qualifiedSignature,modifiersFrom(modifiers));
    }
    
    public static MethodInfo from(Method method) {
        return new MethodInfo(ReflectionUtils.getQualifiedSignature(method), method.getModifiers());
    }
     
    public static MethodInfo from(String className, String methodName, String typeString, int modifiers) {
        return new MethodInfo(className.replace('.', '/') + "/" + methodName + typeString, modifiers);
    }
    
    public static MethodInfo from(String className, String methodName, String typeString, Iterable<String> modifiers) {
        return from(className, methodName, typeString, modifiersFrom(modifiers));
    }
    
    public MethodInfo forClass(String className) {
        return new MethodInfo(className+"/"+getSignature(), modifiers);
    }
    
    public String getSignature() {
        return qualifiedSignature.substring(methodDelim+1);
    }

    public String getBinaryClassName() {
        return qualifiedSignature.substring(0,methodDelim);
    }

    public String getMemberName() {
        return qualifiedSignature.substring(methodDelim+1,typeDelim);
    }

    public String getType() {
        return qualifiedSignature.substring(typeDelim);
    }

    public boolean isSynchronized() {
        return Modifier.isSynchronized(modifiers);
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }
    
    public boolean isNative() {
        return Modifier.isNative(modifiers);
    }
    
    @Override
    public int hashCode() {
        return sigAndModsHashcode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MethodInfo) && sigAndModsEquals((MethodInfo)obj);
    }

    @Override
    public String toString() {
        return "MethodInfo: "+getQualifiedSignature();
    }
}
