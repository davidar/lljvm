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


public class ClassName {
    private final String binaryName;
    private final String javaName;
    private final int simplePos;
    
    
    
    public ClassName(String name) {
        if (name==null || name.isEmpty())
            throw new IllegalArgumentException();
        this.binaryName = name.replace('.', '/');
        this.javaName = name.replace('/', '.');
        int p = javaName.lastIndexOf('.');
        simplePos = p<0 ? 0 : p+1;
    }
    
    public static ClassName from(String name) {
        return new ClassName(name);
    }
    
    public static ClassName from(Class<?> cls) {
        return from(cls.getName());
    }
    
    public String getBinaryName() {
        return binaryName;
    }


    public String getJavaName() {
        return javaName;
    }
    
    public String getSimpleName() {
        return javaName.substring(simplePos);
    }

    public String getBinaryPackageName() {
        return simplePos>0 ? binaryName.substring(0,simplePos-1) : "";
    }
 
    public String getJavaPackageName() {
        return simplePos>0 ? binaryName.substring(0,simplePos-1) : "";
    }

    public boolean hasSamePackageAs(ClassName other) {
        return getBinaryPackageName().equals(other.getBinaryPackageName());
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
