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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lljvm.util.ReflectionUtils;
import lljvm.util.ReflectionUtils.FieldInfo;
import lljvm.util.ReflectionUtils.MethodInfo;

/**
 * Default resolver implementation. 
 * @author Joshua Arnold
 */
public class DefaultResolver implements Resolver {
    
    /**
     * List of classes to search when resolving unqualified references.
     */
    private final List<ClassName> implicitClasses;
    
    /**
     * Optional class name to link any unresolvable references to.  If <code>null</code>, then unresolvable references
     * will result in an {@link AsmLinkerException} being thrown. 
     */
    private final ClassName unresolvedClassName;
    
    /**
     * The class loader to use to load classes being linked to.
     */
    private final ClassLoader loader;
    
    /**
     * Cache of class information by name.
     */
    private final ConcurrentHashMap<ClassName,ClassInfo> infoByClass = new ConcurrentHashMap<ClassName, DefaultResolver.ClassInfo>();

    
    /**
     * Constructor
     * @param implicitClasses list of class names to search when resolving uqualified references
     * @param unresolvedClassName if not <code>null</code>, then any references that can't be resolved will be
     * statically linked to this class name.  This will allow the link to succeed even if some references are unresolvable.
     * Following such references at runtime will typically result in a {@link LinkageError} of some sort, unless the corresponding
     * members actually exist on the <code>unresolvedClassName</code>.
     * @param loader the class loader to use to load classes for inspection.
     */
    public DefaultResolver(List<String> implicitClasses, String unresolvedClassName, ClassLoader loader) {
        super();
        List<ClassName> imps = new ArrayList<DefaultResolver.ClassName>(implicitClasses.size());
        for(String implicitClass : implicitClasses) {
            imps.add(classNameFor(implicitClass));
        }
        this.implicitClasses = Collections.unmodifiableList(imps);
        this.unresolvedClassName = unresolvedClassName!=null ? classNameFor(unresolvedClassName) : null;
        this.loader = loader;
    }
    
    
    private ClassInfo getInfo(ClassName className) {
        ClassInfo res = infoByClass.get(className);
        if (res==null) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className.javaName, false, loader);
            } catch (ClassNotFoundException e) {
                throw new AsmLinkerException("Unable to resolve "+className,e);
            } catch (LinkageError e) {
                throw new AsmLinkerException("Unable to load "+className,e);
            }
            res = new ClassInfo(ReflectionUtils.buildMethodMap(clazz), ReflectionUtils.buildFieldMap(clazz));
            ClassInfo orig = infoByClass.putIfAbsent(className, res);
            if (orig!=null)
                res = orig;
        }
        return res;
    }

    @Override
    public MethodReference resolveMethod(String sig) {
        MethodSymRef symRef = methodSymRef(sig);
        MethodInfo info = null;
        
        if (symRef.target == null) {
            for(ClassName cn : implicitClasses) {
                info = getInfo(cn).methodsBySignature.get(symRef.signature);
                if (info!=null) {
                    symRef = symRef.forTarget(cn);
                    break;
                }
            }
            if (info==null && unresolvedClassName!=null) {
                return new MethodReference(symRef.forTarget(unresolvedClassName).symRef,MethodReference.InvocationType.STATIC,null);
            }
        } else {
            ClassInfo ci = getInfo(classNameFor(symRef.target));
            info = ci.methodsBySignature.get(symRef.signature);
        }
        if (info==null)
            throw new AsmLinkerException("Unable to resolve method signature <"+sig+">");
        MethodReference.InvocationType invocType;
        if (Modifier.isStatic(info.method.getModifiers()))
            invocType = MethodReference.InvocationType.STATIC;
        else if (info.targetClass.isInterface())
            invocType = MethodReference.InvocationType.INTERFACE;
        else
            invocType = MethodReference.InvocationType.VIRTUAL;
        return new MethodReference(symRef.symRef, invocType, null);
    }

    @Override
    public MethodReference resolveLocalMethod(String sig, String className) {
        MethodSymRef symRef = methodSymRef(sig);
        if (symRef.target==null)
            symRef = symRef.forTarget(classNameFor(className));
        return new MethodReference(symRef.symRef, MethodReference.InvocationType.VIRTUAL, EnumSet.of(MethodReference.Flags.LOCAL));
    }

    @Override
    public FieldReference resolveField(String sig) {
        FieldSymRef symRef = fieldSymRef(sig);
        FieldInfo info = null;
        
        if (symRef.target == null) {
            for(ClassName cn : implicitClasses) {
                info = getInfo(cn).fieldsBySignature.get(symRef.signature);
                if (info!=null) {
                    symRef = symRef.forTarget(cn);
                    break;
                }
            }
            if (info==null && unresolvedClassName!=null) {
                return new FieldReference(symRef.forTarget(unresolvedClassName).symRef,FieldReference.AccessType.STATIC,null);
            }
        } else {
            info = getInfo(classNameFor(symRef.target)).fieldsBySignature.get(symRef.signature);
        }
        if (info==null)
            throw new AsmLinkerException("Unable to resolve field signature <"+sig+">");
        FieldReference.AccessType accessType;
        if (Modifier.isStatic(info.field.getModifiers()))
            accessType = FieldReference.AccessType.STATIC;
        else
            accessType = FieldReference.AccessType.INSTANCE;
        return new FieldReference(symRef.symRef, accessType, null);
    }

    @Override
    public FieldReference resolveLocalField(String sig, String className) {
        FieldSymRef symRef = fieldSymRef(sig);
        if (symRef.target==null)
            symRef = symRef.forTarget(classNameFor(className));
        return new FieldReference(symRef.symRef, FieldReference.AccessType.INSTANCE, EnumSet.of(FieldReference.Flags.LOCAL));
    }

    private static class ClassInfo {
        final Map<String,MethodInfo> methodsBySignature;
        final Map<String,FieldInfo> fieldsBySignature;
        ClassInfo(Map<String, MethodInfo> methodsBySignature,
                Map<String, FieldInfo> fieldsBySignature) {
            super();
            this.methodsBySignature = unmodifiableCopy(methodsBySignature);
            this.fieldsBySignature = unmodifiableCopy(fieldsBySignature);
        }
        
    }
    
    
    private static ClassName classNameFor(String s) {
        return new ClassName(s);
    }
    
    /**
     * Convenience class for translating between java names and binary names.
     */
    private static final class ClassName {
        final String binaryName;
        final String javaName;
        ClassName(String name) {
            this.binaryName = name.replace('.', '/');
            this.javaName = name.replace('/', '.');
        }
        @Override
        public boolean equals(Object o) {
            return (o instanceof ClassName) && ((ClassName)o).javaName.equals(javaName);
        }
        @Override
        public int hashCode() {
            return javaName.hashCode();
        }
        @Override
        public String toString() {
            return javaName;
        }
        
    }
    
    static FieldSymRef fieldSymRef(String fsr) {
        return new FieldSymRef(fsr);
    }
    
    private static class FieldSymRef {
        final String target;
        final String signature;
        final String symRef;
        FieldSymRef(String symRef) {
            this.symRef = symRef;
            int p = symRef.indexOf(' ');
            p = symRef.lastIndexOf('/', p >= 0 ? p : symRef.length());
            if (p<0) {
                signature = symRef;
                target = null;
            } else {
                signature = symRef.substring(p+1);
                target = symRef.substring(0,p);
            }
        }
        FieldSymRef forTarget(ClassName target) {
            return new FieldSymRef(target.binaryName+'/'+signature);
        }

    }
    
    static MethodSymRef methodSymRef(String fsr) {
        return new MethodSymRef(fsr);
    }

    private static class MethodSymRef {
        final String target;
        final String signature;
        final String symRef;
        MethodSymRef(String symRef) {
            this.symRef = symRef;
            int p = symRef.indexOf('(');
            p = symRef.lastIndexOf('/', p >= 0 ? p : symRef.length());
            if (p<0) {
                signature = symRef;
                target = null;
            } else {
                signature = symRef.substring(p+1);
                target = symRef.substring(0,p);
            }
        }
        MethodSymRef forTarget(ClassName target) {
            return new MethodSymRef(target.binaryName+'/'+signature);
        }
    }
    
    static String firstPiece(String src, String delim) {
        int p =src.indexOf(delim);
        return p>=0 ? src.substring(0,p) : src;
    }

    static <K,V> Map<K,V> unmodifiableCopy(Map<K,V>  src) {
        return Collections.unmodifiableMap(new HashMap<K, V>(src));
    }

}
