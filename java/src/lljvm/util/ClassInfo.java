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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lljvm.util.ReflectionUtils.modifiersFrom;

/**
 * Contains basic information about a class.
 * <p>
 * The primary advantage of {@code ClassInfo} over {@link Class} is that the former doesn't
 * require the class to actually be loaded.   For example, the ASM linker uses {@code ClassInfo}
 * to describe classes defined in an ASM source file.
 * </p> 
 * @author Joshua Arnold
 */
public final class ClassInfo {
    private final ClassName name;
    private final List<MethodInfo> methods;
    private final List<FieldInfo> fields;
    private final int modifiers;
    private int hash;
    
    private final Map<String, MethodInfo> methodsBySignature;
    private final Map<String, FieldInfo> fieldsBySignature;
    
    
    private static <V extends MemberInfo> Map<String, V> createMemberMap(Collection<? extends V> items) {
        HashMap<String, V> map = new HashMap<String, V>();
        for(V item : items) {
            String uqr = item.getSignature();
            if (!map.containsKey(uqr))
                map.put(uqr,item);
        }
        return Collections.unmodifiableMap(map);
    }
    
    public ClassInfo(ClassName name, Collection<? extends MethodInfo> methods, Collection<? extends FieldInfo> fields, int modifiers) {
        super();
        if (name==null)
            throw new IllegalArgumentException();
        this.name = name;
        this.methods = copy(methods);
        this.fields = copy(fields);
        this.modifiers = modifiers;
        this.methodsBySignature = createMemberMap(methods);
        this.fieldsBySignature = createMemberMap(fields);
    }
    public ClassInfo(ClassName name, Collection<? extends MethodInfo> methods, Collection<? extends FieldInfo> fields, String...modifiers) {
        this(name,methods,fields,modifiersFrom(modifiers));
    }
    public ClassInfo(ClassName name, Collection<? extends MethodInfo> methods, Collection<? extends FieldInfo> fields, Iterable<String> modifiers) {
        this(name,methods,fields,modifiersFrom(modifiers));
    }

    
    public ClassName getName() {
        return name;
    }
    
    public String getBinaryName() {
        return name.getBinaryName();
    }
    
    public String getJavaName() {
        return name.getJavaName();
    }
    
    public List<MethodInfo> getMethods() {
        return methods;
    }
    public List<FieldInfo> getFields() {
        return fields;
    }
    public int getModifiers() {
        return modifiers;
    }

    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }
    
    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }
    
    public boolean isInterface() {
        return Modifier.isInterface(modifiers);
    }

    private static <T> List<T> copy(Collection<? extends T> col) {
        return col != null ? Collections.unmodifiableList(new ArrayList<T>(col)) : Collections.<T>emptyList();
    }
    
    public MethodInfo findMethod(String unqualifiedRef) {
        return methodsBySignature.get(unqualifiedRef);
    }
    public FieldInfo findField(String unqualifiedRef) {
        return fieldsBySignature.get(unqualifiedRef);
    }
    
    public Map<String, MethodInfo> getMethodMap() {
        return methodsBySignature;
    }
    public Map<String, FieldInfo> getFieldMap() {
        return fieldsBySignature;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h==0) {
            h = modifiers + 757 * (
                    name.hashCode() + 757 * (
                            methodsBySignature.hashCode() + 757 * fieldsBySignature.hashCode()));
            h = h==0 ? -1 : h;
            hash = h;                  
        }
        return h;
    }

    /**
     * Defines equality of {@code ClassInfo} instances.
     * @return true if {@code obj} is a {@code ClassInfo} and has the same {@linkplain #getName() name}, {@linkplain #getModifiers() modifiers},
     * {@linkplain #getFields() fields}, and {@link #getMethods() methods} as this {@code ClassInfo}.  The order of methods and fields in their
     * respective lists is not considered relevant.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj==this)
            return true;
        if (!(obj instanceof ClassInfo))
            return false;
        ClassInfo ci = (ClassInfo)obj;
        return modifiers==ci.modifiers && name.equals(ci.name) 
            && methodsBySignature.equals(ci.methodsBySignature) 
            && fieldsBySignature.equals(ci.fieldsBySignature);
    }

    @Override
    public String toString() {
        return "ClassInfo: "+getName();
    }

}
