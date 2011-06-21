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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static lljvm.util.ReflectionUtils.modifiersFrom;

/**
 * Contains basic information about a field.
 * <p>
 * The primary advantage of {@code FieldInfo} over {@link Field} is that the former doesn't
 * require the field's class to be loaded.   For example, the ASM linker uses {@code FieldInfo}
 * to describe fields defined in an ASM source file.
 * </p> 
 * @author Joshua Arnold
 */
public final class FieldInfo extends MemberInfo {
    
    private final int fieldDelim;
    private final int typeDelim;

    public FieldInfo(String qualifiedSignature, int modifiers) {
        super(qualifiedSignature, modifiers);

        typeDelim = qualifiedSignature.indexOf(' ');
        if (typeDelim < 0)
            throw new IllegalArgumentException(qualifiedSignature);

        fieldDelim = qualifiedSignature.lastIndexOf('/', typeDelim-1);
        if (fieldDelim < 0)
            throw new IllegalArgumentException(qualifiedSignature);
    }
    
    public FieldInfo(String qualifiedSignature, Iterable<String> modifiers) {
        this(qualifiedSignature,modifiersFrom(modifiers));
    }
    
    public FieldInfo(String qualifiedSignature, String... modifiers) {
        this(qualifiedSignature,modifiersFrom(modifiers));
    }
    
    public static FieldInfo from(Field field) {
        return new FieldInfo(ReflectionUtils.getQualifiedSignature(field), field.getModifiers());
    }
    
    public static FieldInfo from(String className, String fieldName, String typeString, int modifiers) {
        return new FieldInfo(className.replace('.', '/') + "/" + fieldName +" "+typeString, modifiers);
    }
    
    public static FieldInfo from(String className, String fieldName, String typeString, Iterable<String> modifiers) {
        return from(className, fieldName, typeString, modifiersFrom(modifiers));
    }
    
    public FieldInfo forClass(String className) {
        return new FieldInfo(className+"/"+getSignature(), modifiers);
    }

    public String getSignature() {
        return qualifiedSignature.substring(fieldDelim+1);
    }

    public String getBinaryClassName() {
        return qualifiedSignature.substring(0,fieldDelim);
    }

    public String getMemberName() {
        return qualifiedSignature.substring(fieldDelim+1,typeDelim);
    }

    public String getType() {
        return qualifiedSignature.substring(typeDelim+1);
    }

    public boolean isVolatile() {
        return Modifier.isVolatile(modifiers);
    }

    public boolean isTransient() {
        return Modifier.isTransient(modifiers);
    }

    @Override
    public int hashCode() {
        return sigAndModsHashcode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FieldInfo) && sigAndModsEquals((FieldInfo)obj);
    }

    @Override
    public String toString() {
        return "FieldInfo: "+getQualifiedSignature();
    }
    

}
