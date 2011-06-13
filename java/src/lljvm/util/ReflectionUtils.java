/*
* Copyright (c) 2009 David Roberts <d@vidr.cc>
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

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides methods for obtaining reflective information about classes.
 * 
 * @author  David Roberts
 */
public final class ReflectionUtils {
    /**
     * Prevent this class from being instantiated.
     */
    private ReflectionUtils() {}
    
    /**
     * Returns the class with the specified binary name.
     * 
     * @param name  the binary name of the class to return
     * @return      the class with the specified binary name
     * @throws ClassNotFoundException
     *              if the class cannot be found
     */
    public static Class<?> getClass(String name)
    throws ClassNotFoundException {
        name = name.replace('/', '.');
        ClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(
                    new URL[] { new File(".").toURI().toURL() });
        } catch(MalformedURLException e) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return classLoader.loadClass(name);
    }
    
    /**
     * Returns a list of the static methods contained in the given array of
     * methods.
     * 
     * @param allMethods  the array of methods to filter
     * @return            the list of static methods
     */
    private static List<Method> getStaticMethods(Method[] allMethods) {
        List<Method> methods = new ArrayList<Method>();
        for(Method method : allMethods)
            if(Modifier.isStatic(method.getModifiers()))
                methods.add(method);
        return methods;
    }
    
    private static List<Method> getInstanceMethods(Method[] allMethods) {
        List<Method> methods = new ArrayList<Method>();
        for(Method method : allMethods)
            if(!Modifier.isStatic(method.getModifiers()))
                methods.add(method);
        return methods;
    	
    }
    
    /**
     * Returns a list of the public static methods provided by the specified
     * class.
     * 
     * @param cls  the class providing the methods
     * @return     a list of the public static methods provided by the
     *             specified class
     */
    public static List<Method> getPublicStaticMethods(Class<?> cls) {
        return getStaticMethods(cls.getMethods());
    }
    
    
    /**
     * Returns a list of the static methods provided by the specified class.
     * 
     * @param cls  the class providing the methods
     * @return     a list of the static methods provided by the specified class
     */
    public static List<Method> getStaticMethods(Class<?> cls) {
        return getStaticMethods(cls.getDeclaredMethods());
    }
    
    public static List<Method> getPublicInstanceMethods(Class<?> cls) {
    	return getInstanceMethods(cls.getMethods());
    }

    public static List<Method> getAllPublicMethods(Class<?> cls) {
    	return new ArrayList<Method>(Arrays.asList(cls.getMethods()));
    }

    public static List<Method> getAllMethods(Class<?> cls) {
    	return new ArrayList<Method>(Arrays.asList(cls.getMethods()));
    }

    /**
     * Returns a list of the public static fields provided by the specified
     * class.
     * 
     * @param cls  the class providing the fields
     * @return     a list of the public static fields provided by the
     *             specified class
     */
    public static List<Field> getPublicStaticFields(Class<?> cls) {
        List<Field> fields = new ArrayList<Field>();
        for(Field field : cls.getFields()) {
            int modifiers = field.getModifiers();
            if(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
                fields.add(field);
        }
        return fields;
    }

    /**
     * Returns a list of the public non-static fields provided by the specified
     * class.
     * 
     * @param cls  the class providing the fields
     * @return     a list of the public static fields provided by the
     *             specified class
     */
    public static List<Field> getPublicInstanceFields(Class<?> cls) {
        List<Field> fields = new ArrayList<Field>();
        for(Field field : cls.getFields()) {
            int modifiers = field.getModifiers();
            if(Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers))
                fields.add(field);
        }
        return fields;
    }    
    
    public static List<Field> getAllPublicFields(Class<?> cls) {
        List<Field> fields = new ArrayList<Field>();
        for(Field field : cls.getFields()) {
            int modifiers = field.getModifiers();
            if(Modifier.isPublic(modifiers))
                fields.add(field);
        }
        return fields;
    }
    
    /**
     * Returns the type descriptor of the specified type.
     * 
     * @param cls  the Class representing the specifed type
     * @return     the type descriptor of the specified type
     */
    public static String getDescriptor(Class<?> cls) {
        if(cls == void.class)    return "V";
        if(cls == boolean.class) return "Z";
        if(cls == byte.class)    return "B";
        if(cls == char.class)    return "C";
        if(cls == short.class)   return "S";
        if(cls == int.class)     return "I";
        if(cls == long.class)    return "J";
        if(cls == float.class)   return "F";
        if(cls == double.class)  return "D";
        if(cls.isArray())
            return cls.getName().replace('.', '/');
        return "L"+cls.getName().replace('.', '/')+";";
    }
    
    /**
     * Returns the type signature of the given method.
     * 
     * @param method  the given method
     * @return        the type signature of the given method
     */
    public static String getSignature(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getName());
        builder.append("(");
        for(Class<?> param : method.getParameterTypes())
            builder.append(getDescriptor(param));
        builder.append(")");
        builder.append(getDescriptor(method.getReturnType()));
        return builder.toString();
    }
    
    /**
     * Returns the qualified signature (i.e. including the name of the parent
     * class) of the given method.
     * 
     * @param method  the given method
     * @return        the qualified signature of the given method
     */
    public static String getQualifiedSignature(Method method) {
        return method.getDeclaringClass().getName().replace('.', '/')
                + "/" + getSignature(method);
    }
    
    /**
     * Returns the type signature of the given field.
     * 
     * @param field  the given field
     * @return       the type signature of the given field
     */
    public static String getSignature(Field field) {
        return field.getName() + " " + getDescriptor(field.getType());
    }
    
    /**
     * Convenience method for getting the type signature of a Field or Method
     * object.
     * 
     * @param o  the Field or Method object
     * @return   the type signature
     */
    public static String getSignature(AccessibleObject o) {
        if(o instanceof Field)
            return getSignature((Field) o);
        if(o instanceof Method)
            return getSignature((Method) o);
        throw new IllegalArgumentException(
                "Only Field and Method objects allowed");
    }
    

    public static Map<String, MethodInfo> buildMethodMap(Class<?> clazz) {
        Map<String, MethodInfo> map = new HashMap<String, MethodInfo>();
        String binName = clazz.getName().replace('.', '/');
        for(Method method : getAllPublicMethods(clazz)) {
            String methodSig = getSignature(method);
            if(!map.containsKey(methodSig))
                map.put(methodSig, new MethodInfo(clazz, binName+"/"+methodSig, method));
        }
        return map;
    }

    public static Map<String, FieldInfo> buildFieldMap(Class<?> clazz) {
        Map<String, FieldInfo> map = new HashMap<String, FieldInfo>();
        String binName = clazz.getName().replace('.', '/');
        for(Field field : getAllPublicFields(clazz)) {
            String fieldSig = getSignature(field);
            if(!map.containsKey(fieldSig))
                map.put(fieldSig, new FieldInfo(clazz, binName+"/"+fieldSig, field));
        }
        return map;
    }


    
    public static final class MethodInfo {
        public final Class<?> targetClass;
        public final String symbolicReference;
        public final Method method;
        public MethodInfo(Class<?> targetClass, String symbolicReference, Method method) {
            super();
            this.targetClass = targetClass;
            this.symbolicReference = symbolicReference;
            this.method = method;
        }        
        
        public String targetClassBinaryName() {
            int p = symbolicReference.indexOf('(');
            p = symbolicReference.lastIndexOf('/', p);
            return symbolicReference.substring(0,p);
        }
    }
    
    public static final class FieldInfo {
        public final Class<?> targetClass;
        public final String symbolicReference;
        public final Field field;
        public FieldInfo(Class<?> targetClass, String symbolicReference, Field field) {
            super();
            this.targetClass = targetClass;
            this.symbolicReference = symbolicReference;
            this.field = field;
        }        
        public String targetClassBinaryName() {
            int p = symbolicReference.indexOf(' ');
            p = symbolicReference.lastIndexOf('/', p);
            return symbolicReference.substring(0,p);
        }

    }
    
    public static int sizeOf(Class<?> cls) {
        if(cls == boolean.class) return 1;
        if(cls == byte.class)    return 1;
        if(cls == char.class)    return 2;
        if(cls == short.class)   return 2;
        if(cls == int.class)     return 4;
        if(cls == long.class)    return 8;
        if(cls == float.class)   return 4;
        if(cls == double.class)  return 8;
        throw new IllegalArgumentException(
                "Cannot request size of non-primitive type");
    }
}
