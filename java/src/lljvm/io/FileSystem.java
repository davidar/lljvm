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

/**
 * An interface for interacting with the file system.
 * 
 * @author  David Roberts
 */
public interface FileSystem {
    /**
     * Create and open a file or device.
     * 
     * @param pathname  the name of the file
     * @param flags     the file status flags
     * @param mode      the permissions for the newly created file
     * @return          the new file handle
     */
    public FileHandle open(String pathname, int flags, int mode);
    
    /**
     * Open a file or device.
     * 
     * @param pathname  the name of the file
     * @param flags     the file status flags
     * @return          the new file handle
     */
    public FileHandle open(String pathname, int flags);
    
    /**
     * Change the name or location of a file.
     * 
     * @param oldpath  the current path of the file
     * @param newpath  the new path of the file
     * @return         true on success
     */
    public boolean rename(String oldpath, String newpath);
    
    /**
     * Create a new (hard) link to an existing file.
     * 
     * @param oldpath  the existing file
     * @param newpath  the link to be created, unless newpath already exists
     * @return         true on success
     */
    public boolean link(String oldpath, String newpath);
    
    /**
     * Delete a name from the file system.
     * 
     * @param pathname  the name to delete
     * @return          true on success
     */
    public boolean unlink(String pathname);
    
    /**
     * Change the working directory.
     * 
     * @param path  the new working directory
     * @return      true on success
     */
    public boolean chdir(String path);
    
    /**
     * Return the absolute pathname of the current working directory.
     * 
     * @return  the current working directory
     */
    public String getcwd();
}
