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

package lljvm.io;

import java.io.File;
import java.io.IOException;

import lljvm.runtime.Error;
import lljvm.runtime.IO;

/**
 * Implements the FileSystem interface using the native Java I/O operations.
 * 
 * @author  David Roberts
 */
public class NativeFileSystem implements FileSystem {
    /** User working directory system property */
    private static final String USER_DIR = System.getProperty("user.dir");
    /** Current working directory */
    private File cwd = (USER_DIR == null ? new File("") : new File(USER_DIR));
    
    /**
     * Return the File object representing the file with the given name,
     * relative to the current working directory if applicable.
     * 
     * @param name  the name of the file
     * @return      the File object representing the file
     */
    private File newFile(String name) {
        File file = new File(name);
        if(!file.isAbsolute())
            file = new File(cwd, name);
        return file;
    }
    
    public FileHandle open(String pathname, int flags, int mode) {
        File file = newFile(pathname);
        try {
            if(file.createNewFile()) {
                file.setReadable(
                        (mode & (IO.S_IRUSR|IO.S_IRGRP|IO.S_IROTH)) != 0,
                        (mode & (IO.S_IRGRP|IO.S_IROTH)) == 0);
                file.setWritable(
                        (mode & (IO.S_IWUSR|IO.S_IWGRP|IO.S_IWOTH)) != 0,
                        (mode & (IO.S_IWGRP|IO.S_IWOTH)) == 0);
                file.setExecutable(
                        (mode & (IO.S_IXUSR|IO.S_IXGRP|IO.S_IXOTH)) != 0,
                        (mode & (IO.S_IXGRP|IO.S_IXOTH)) == 0);
            } else { // file already exists
                if((flags & IO.O_EXCL) != 0) {
                    Error.errno(Error.EEXIST);
                    return null;
                }
            }
        } catch(IOException e) {
            Error.errno(Error.EACCES);
            return null;
        }
        return open(file, flags);
    }
    
    public FileHandle open(String pathname, int flags) {
        return open(newFile(pathname), flags);
    }
    
    /**
     * Open a file handle for the given file.
     * 
     * @param file   the File object representing the file
     * @param flags  the file status flags
     * @return       the new file handle
     */
    private FileHandle open(File file, int flags) {
        try {
            return new RandomAccessFileHandle(file, flags);
        } catch(IOException e) {
            Error.errno(Error.EACCES);
            return null;
        }
    }
    
    public boolean rename(String oldpath, String newpath) {
        File oldfile = newFile(oldpath);
        File newfile = newFile(newpath);
        return oldfile.renameTo(newfile);
    }
    
    public boolean link(String oldpath, String newpath) {
        // TODO: Java 7: <http://java.sun.com/docs/books/tutorial/essential/
        //                                              io/links.html#hardLink>
        return false;
    }
    
    public boolean unlink(String pathname) {
        return false;
    }
    
    public boolean chdir(String path) {
        File newCWD = newFile(path);
        if(!newCWD.exists())
            return false;
        cwd = newCWD;
        return true;
    }
    
    public String getcwd() {
        return cwd.getAbsolutePath();
    }
}
