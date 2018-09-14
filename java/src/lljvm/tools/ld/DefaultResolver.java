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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import lljvm.util.ClassInfo;
import lljvm.util.ClassName;
import lljvm.util.ReflectionUtils;
import lljvm.util.FieldInfo;
import lljvm.util.MethodInfo;

/**
 * Default resolver implementation.
 * 
 * @author Joshua Arnold
 */
public class DefaultResolver implements Resolver {

    private static final Logger logger = Logger.getLogger(DefaultResolver.class.getName());

    /**
     * List of classes to search when resolving unqualified references.
     */
    private final List<ClassName> implicitClasses;

    /**
     * Optional class name to link any unresolvable references to. If <code>null</code>, then
     * unresolvable references will result in an {@link AsmLinkerException} being thrown.
     */
    private final ClassName unresolvedClassName;

    /**
     * The class loader to use to load classes being linked to.
     */
    private final ClassLoader loader;

    private final Map<ClassName, ClassInfo> preLoadedClasses;

    /**
     * Cache of class information by name.
     */
    private final ConcurrentHashMap<ClassName, ClassInfo> additionalClasses;

    private final UnqualifiedResolver<MethodInfo> unqualifiedMethodResolver = new UnqualifiedResolver<MethodInfo>() {
        @Override
        MethodInfo resolve(ClassInfo classInfo, String unqualifiedName) {
            MethodInfo info = classInfo.findMethod(unqualifiedName);
            return info != null && info.isPublic() ? info : null;
        }
    };

    private final UnqualifiedResolver<FieldInfo> unqualifiedFieldResolver = new UnqualifiedResolver<FieldInfo>() {
        @Override
        FieldInfo resolve(ClassInfo classInfo, String unqualifiedName) {
            FieldInfo info = classInfo.findField(unqualifiedName);
            return info != null && info.isPublic() ? info : null;
        }
    };

    /**
     * Constructor
     * 
     * @param implicitClasses
     *            list of class names to search when resolving uqualified references
     * @param unresolvedClassName
     *            if not <code>null</code>, then any references that can't be resolved will be
     *            statically linked to this class name. This will allow the link to succeed even if
     *            some references are unresolvable. Following such references at runtime will
     *            typically result in a {@link LinkageError} of some sort, unless the corresponding
     *            members actually exist on the <code>unresolvedClassName</code>.
     * @param loader
     *            the class loader to use to load classes for inspection.
     */
    public DefaultResolver(Iterable<? extends ClassInfo> resolvedClasses,
            Iterable<? extends ClassName> implicitClasses, ClassName unresolvedClassName,
            ClassLoader loader) {
        super();
        this.loader = loader;
        this.unresolvedClassName = unresolvedClassName;

        HashMap<ClassName, ClassInfo> prel = new HashMap<ClassName, ClassInfo>();
        for (ClassInfo resolved : resolvedClasses) {
            ClassName name = resolved.getName();
            if (!prel.containsKey(name)) {
                prel.put(name, resolved);
            } else {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest(String
                            .format("Multiple ClassInfo objects found for %s - only the first will be used",
                                    name));
            }
        }
        this.preLoadedClasses = Collections.unmodifiableMap(prel);

        List<ClassName> imps = new ArrayList<ClassName>();
        for (ClassName implicitClass : implicitClasses) {
            imps.add(implicitClass);
        }
        this.implicitClasses = Collections.unmodifiableList(imps);

        int mcnt = 0;
        int fcnt = 0;
        for (ClassInfo ci : preLoadedClasses.values()) {
            mcnt += ci.getMethodMap().size();
            fcnt += ci.getFieldMap().size();
        }

        additionalClasses = new ConcurrentHashMap<ClassName, ClassInfo>(128, 0.75f, 2 * Runtime
                .getRuntime().availableProcessors());

    }

    ClassInfo getInfo(ClassName className) {
        ClassInfo res = preLoadedClasses.get(className);
        if (res != null)
            return res;
        res = additionalClasses.get(className);
        if (res == null) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className.getJavaName(), false, loader);
            } catch (ClassNotFoundException e) {
                throw new AsmLinkerException("Unable to resolve " + className, e);
            } catch (LinkageError e) {
                throw new AsmLinkerException("Unable to load " + className, e);
            }
            res = ReflectionUtils.infoFor(clazz);
            ClassInfo orig = additionalClasses.putIfAbsent(className, res);
            if (orig != null)
                res = orig;
        }
        return res;
    }

    List<ClassName> getImplicitClasses() {
        return implicitClasses;
    }

    @Override
    public MethodReference resolveMethod(String sig, ClassInfo referrer) {
        MethodSignature parsedSig = methodSignature(sig);
        MethodInfo methodInfo = null;
        ClassInfo classInfo = null;

        if (parsedSig.target == null) {
            Pair<ClassInfo, MethodInfo> res = unqualifiedMethodResolver
                    .resolve(parsedSig.unqualifiedSignature);
            if (res != null) {
                classInfo = res.first;
                methodInfo = res.second;
            } else if (unresolvedClassName != null) {
                return new MethodReference(parsedSig.forTarget(unresolvedClassName).signature,
                        MethodReference.InvocationType.STATIC, null);
            }
        } else {
            classInfo = getInfo(classNameFor(parsedSig.target));
            methodInfo = classInfo.findMethod(parsedSig.unqualifiedSignature);
        }

        if (methodInfo == null || !methodInfo.isPublic())
            throw new AsmLinkerException("Unable to resolve public method signature <" + sig + ">");
        MethodReference.InvocationType invocType;
        if (methodInfo.isStatic())
            invocType = MethodReference.InvocationType.STATIC;
        else if (classInfo.isInterface())
            invocType = MethodReference.InvocationType.INTERFACE;
        else
            invocType = MethodReference.InvocationType.VIRTUAL;
        return new MethodReference(methodInfo.getQualifiedSignature(), invocType, null);
    }

    @Override
    public MethodReference resolveLocalMethod(String sig, ClassInfo referrer) {
        if (referrer.isInterface())
            throw new IllegalArgumentException("Referrer must be a class");
        MethodSignature symRef = methodSignature(sig);
        MethodInfo info = referrer.findMethod(symRef.unqualifiedSignature);
        if (info == null) {
            throw new AsmLinkerException("Local method not found: " + sig);
        }
        MethodReference.InvocationType invocType;
        if (info.isStatic())
            invocType = MethodReference.InvocationType.STATIC;
        else if (info.isPrivate())
            invocType = MethodReference.InvocationType.SPECIAL;
        else
            invocType = MethodReference.InvocationType.VIRTUAL;
        return new MethodReference(info.getQualifiedSignature(), invocType,
                EnumSet.of(MethodReference.Flags.LOCAL));
    }

    @Override
    public FieldReference resolveField(String sig, ClassInfo referrer) {
        FieldSignature parsedSig = fieldSignature(sig);
        FieldInfo fieldInfo = null;

        if (parsedSig.target == null) {
            Pair<ClassInfo, FieldInfo> res = unqualifiedFieldResolver
                    .resolve(parsedSig.unqualifiedSignature);
            if (res != null) {
                fieldInfo = res.second;
            } else if (unresolvedClassName != null) {
                return new FieldReference(parsedSig.forTarget(unresolvedClassName).signature,
                        FieldReference.AccessType.STATIC, null);
            }
        } else {
            fieldInfo = getInfo(classNameFor(parsedSig.target)).findField(
                    parsedSig.unqualifiedSignature);
        }
        if (fieldInfo == null || !fieldInfo.isPublic())
            throw new AsmLinkerException("Unable to resolve public field signature <" + sig + ">");
        FieldReference.AccessType accessType;
        if (fieldInfo.isStatic())
            accessType = FieldReference.AccessType.STATIC;
        else
            accessType = FieldReference.AccessType.INSTANCE;
        return new FieldReference(fieldInfo.getQualifiedSignature(), accessType, null);
    }

    @Override
    public FieldReference resolveLocalField(String sig, ClassInfo referrer) {
        if (referrer.isInterface())
            throw new IllegalArgumentException("Referrer must be a class");
        FieldSignature symRef = fieldSignature(sig);
        FieldInfo info = referrer.findField(symRef.unqualifiedSignature);
        if (info == null) {
            throw new AsmLinkerException("Local field not found: " + sig);
        }
        FieldReference.AccessType accessType;
        if (info.isStatic())
            accessType = FieldReference.AccessType.STATIC;
        else
            accessType = FieldReference.AccessType.INSTANCE;
        return new FieldReference(symRef.signature, accessType,
                EnumSet.of(FieldReference.Flags.LOCAL));
    }

    private static ClassName classNameFor(String s) {
        return new ClassName(s);
    }

    static FieldSignature fieldSignature(String fsr) {
        return new FieldSignature(fsr);
    }

    private static final class FieldSignature {
        final String target;
        final String unqualifiedSignature;
        final String signature;

        FieldSignature(String sig) {
            this.signature = sig;
            int p = sig.indexOf(' ');
            p = sig.lastIndexOf('/', p >= 0 ? p : sig.length());
            if (p < 0) {
                unqualifiedSignature = sig;
                target = null;
            } else {
                unqualifiedSignature = sig.substring(p + 1);
                target = sig.substring(0, p);
            }
        }

        FieldSignature forTarget(ClassName target) {
            return new FieldSignature(target.getBinaryName() + '/' + unqualifiedSignature);
        }

    }

    static MethodSignature methodSignature(String fsr) {
        return new MethodSignature(fsr);
    }

    private static final class MethodSignature {
        final String target;
        final String unqualifiedSignature;
        final String signature;

        MethodSignature(String sig) {
            this.signature = sig;
            int p = sig.indexOf('(');
            p = sig.lastIndexOf('/', p >= 0 ? p : sig.length());
            if (p < 0) {
                unqualifiedSignature = sig;
                target = null;
            } else {
                unqualifiedSignature = sig.substring(p + 1);
                target = sig.substring(0, p);
            }
        }

        MethodSignature forTarget(ClassName target) {
            return new MethodSignature(target.getBinaryName() + '/' + unqualifiedSignature);
        }
    }

    private abstract class UnqualifiedResolver<T> {
        private final ConcurrentHashMap<String, Pair<ClassInfo, T>> resolved = new ConcurrentHashMap<String, Pair<ClassInfo, T>>(
                1024, 0.75f, Runtime.getRuntime().availableProcessors());

        final Pair<ClassInfo, T> resolve(String unqualifiedName) {
            Pair<ClassInfo, T> res = resolved.get(unqualifiedName);
            if (res != null)
                return res;
            for (ClassName cn : DefaultResolver.this.getImplicitClasses()) {
                ClassInfo ci = DefaultResolver.this.getInfo(cn);
                T info = resolve(ci, unqualifiedName);
                if (info != null) {
                    res = Pair.of(ci, info);
                    break;
                }
            }
            if (res==null)
                return null;
            Pair<ClassInfo, T> chk = resolved.putIfAbsent(unqualifiedName, res);
            return chk == null ? res : chk;
        }

        abstract T resolve(ClassInfo classInfo, String unqualifiedName);
    }

}
