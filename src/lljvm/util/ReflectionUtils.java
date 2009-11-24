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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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
     * Returns a list of the public static methods provided by the specified
     * class.
     * 
     * @param cls  the class providing the methods
     * @return     a list of the public static methods provided by the
     *             specified class
     */
    public static List<Method> getPublicStaticMethods(Class<?> cls) {
        List<Method> methods = new ArrayList<Method>();
        for(Method method : cls.getMethods()) {
            int modifiers = method.getModifiers();
            if(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
                methods.add(method);
        }
        return methods;
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
     * Returns the type descriptor of the specified type.
     * 
     * @param cls  the Class representing the specifed type
     * @return     the type descriptor of the specified type
     */
    public static String getDescriptor(Class<?> cls) {
        if(cls == void.class)
            return "V";
        else if(cls == boolean.class)
            return "Z";
        else if(cls == byte.class)
            return "B";
        else if(cls == char.class)
            return "C";
        else if(cls == short.class)
            return "S";
        else if(cls == int.class)
            return "I";
        else if(cls == long.class)
            return "J";
        else if(cls == float.class)
            return "F";
        else if(cls == double.class)
            return "D";
        else if(cls.isArray())
            return cls.getName().replace('.', '/');
        else
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
     * Returns the type signature of the given field.
     * 
     * @param field  the given field
     * @return       the type signature of the given field
     */
    private static String getSignature(Field field) {
        return field.getName() + " " + getDescriptor(field.getType());
    }
    
    /**
     * Given a list of binary names of classes, returns a mapping of type
     * signatures of methods provided by these classes to the binary name of
     * the first class in the given list that provides them.
     * 
     * @param classNames  the list of binary names of classes
     * @return            the mapping of method signatures to class names
     * @throws ClassNotFoundException
     *                    if any of the specified classes cannot be found
     */
    public static Map<String, String> buildMethodMap(List<String> classNames)
    throws ClassNotFoundException {
        Map<String, String> map = new HashMap<String, String>();
        for(String className : classNames) {
            Class<?> cls = getClass(className);
            className = className.replace('.', '/');
            for(Method method : getPublicStaticMethods(cls)) {
                String methodSig = getSignature(method);
                if(!map.containsKey(methodSig))
                    map.put(methodSig, className);
            }
        }
        return map;
    }

    /**
     * Given a list of binary names of classes, returns a mapping of type
     * signatures of fields provided by these classes to the binary name of
     * the first class in the given list that provides them.
     * 
     * @param classNames  the list of binary names of classes
     * @return            the mapping of field signatures to class names
     * @throws ClassNotFoundException
     *                    if any of the specified classes cannot be found
     */
    public static Map<String, String> buildFieldMap(List<String> classNames)
    throws ClassNotFoundException {
        Map<String, String> map = new HashMap<String, String>();
        for(String className : classNames) {
            Class<?> cls = getClass(className);
            className = className.replace('.', '/');
            for(Field field : getPublicStaticFields(cls)) {
                String fieldSig = getSignature(field);
                if(!map.containsKey(fieldSig))
                    map.put(fieldSig, className);
            }
        }
        return map;
    }
}
