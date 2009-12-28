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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Main {
    public static Method getMainMethod(String name)
    throws ClassNotFoundException, NoSuchMethodException {
        return Class.forName(name, false, Main.class.getClassLoader())
                    .getMethod("main", String[].class);
    }
    
    public static boolean hasMainMethod(String name) {
        try {
            getMainMethod(name);
            return true;
        } catch(NoSuchMethodException e) {
            return false;
        } catch(ClassNotFoundException e) {
            return false;
        }
    }
    
    public static void usage() throws IOException {
        File file = new File(Main.class.getProtectionDomain().getCodeSource()
                                       .getLocation().getPath());
        JarFile jar = new JarFile(file);
        System.out.println("USAGE: java -jar "+file.getName()+" cmd args...");
        System.out.println("Where cmd is one of the following:");
        for(Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();) {
            String name = e.nextElement().getName();
            if(name.endsWith(".class") && !name.equals("Main.class")) {
                name = name.substring(0, name.length() - ".class".length());
                if(hasMainMethod(name))
                    System.out.println('\t' + name);
            }
        }
    }    
    
    public static void main(String[] args) {
        try {
            if(args.length < 1) usage();
            else getMainMethod(args[0]).invoke(null, (Object) args);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
