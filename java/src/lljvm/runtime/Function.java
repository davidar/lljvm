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

package lljvm.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lljvm.util.ReflectionUtils;

/**
 * Provides function pointers for methods.
 * 
 * @author  David Roberts
 */
public final class Function {
    /** Set of registered classes */
    private static Set<String> registeredClasses = new HashSet<String>();
    /** Map of function signatures to function pointers */
    private static Map<String, Integer> functionPointers
        = new HashMap<String, Integer>();
    /** Map of function pointers to Method objects */
    private static Map<Integer, Method> functionObjects
        = new HashMap<Integer, Method>();
    
    /**
     * Prevent this class from being instantiated.
     */
    private Function() {}
    
    /**
     * If the class specified by the given binary name has not yet been
     * registered, then generate function pointers for all public static
     * methods that it declares.
     * 
     * @param classname  Binary name of the class to register.
     * @throws ClassNotFoundException
     *                   if no class with the given name can be found
     */
    private static void registerClass(String classname)
    throws ClassNotFoundException {
        if(registeredClasses.contains(classname))
            return;
        Class<?> cls = ReflectionUtils.getClass(classname);
        for(Method method : ReflectionUtils.getStaticMethods(cls)) {
            final int addr = Memory.allocateData();
            final String sig = ReflectionUtils.getQualifiedSignature(method);
            functionPointers.put(sig, addr);
            functionObjects.put(addr, method);
            method.setAccessible(true);
        }
        registeredClasses.add(classname);
    }
    
    /**
     * Return a function pointer for the method with the specified signature
     * declared by the specified class.
     * 
     * @param classname        the binary name of the declaring class
     * @param methodSignature  the signature of the method
     * @return                 a function pointer for the specified method
     */
    public static int getFunctionPointer(String classname,
                                         String methodSignature) {
        try {
            registerClass(classname);
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        final String sig = classname + "/" + methodSignature;
        if(!functionPointers.containsKey(sig))
            throw new IllegalArgumentException(
                    "Unable to get function pointer for "+sig);
        return functionPointers.get(sig);
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     * @return      the return value of the method
     */
    private static Object invoke(int f, int args) {
        final Method method = functionObjects.get(f);
        if(method == null)
            throw new IllegalArgumentException("Invalid function pointer: "+f);
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Object[] params = Memory.unpack(args, paramTypes);
        try {
            return method.invoke(null, params);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch(InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            throw new RuntimeException(cause);
        }
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     */
    public static void invoke_void(int f, int args) {
        invoke(f, args);
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     * @return      the return value of the method
     */
    public static boolean invoke_i1(int f, int args) {
        return (Boolean) invoke(f, args);
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     * @return      the return value of the method
     */
    public static byte invoke_i8(int f, int args) {
        return (Byte) invoke(f, args);
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     * @return      the return value of the method
     */
    public static short invoke_i16(int f, int args) {
        return (Short) invoke(f, args);
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     * @return      the return value of the method
     */
    public static int invoke_i32(int f, int args) {
        return (Integer) invoke(f, args);
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     * @return      the return value of the method
     */
    public static long invoke_i64(int f, int args) {
        return (Long) invoke(f, args);
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     * @return      the return value of the method
     */
    public static float invoke_f32(int f, int args) {
        return (Float) invoke(f, args);
    }
    
    /**
     * Invoke the method pointed to by the given function pointer with the
     * given arguments.
     * 
     * @param f     the function pointer
     * @param args  a pointer to the packed list of arguments
     * @return      the return value of the method
     */
    public static double invoke_f64(int f, int args) {
        return (Double) invoke(f, args);
    }
}
