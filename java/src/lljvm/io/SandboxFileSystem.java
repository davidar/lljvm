package lljvm.io;

import java.io.File;
import java.io.IOException;

import lljvm.runtime.Error;
import lljvm.runtime.IO;
import lljvm.runtime.Environment;

/**
 * Implements the FileSystem interface by disabling all filesystem access.
 * 
 * @author  Theo Julienne
 */
public class SandboxFileSystem implements FileSystem {
    public FileHandle open(Environment env, String pathname, int flags, int mode) {
        env.error.errno(Error.EACCES);
        return null;
    }
    
    public FileHandle open(Environment env, String pathname, int flags) {
        env.error.errno(Error.EACCES);
        return null;
    }

    private FileHandle open(Environment env, File file, int flags) {
        env.error.errno(Error.EACCES);
        return null;
    }
    
    public boolean rename(Environment env, String oldpath, String newpath) {
        return false;
    }
    
    public boolean link(Environment env, String oldpath, String newpath) {
        return false;
    }
    
    public boolean unlink(Environment env, String pathname) {
        return false;
    }
    
    public boolean chdir(Environment env, String path) {
        return false;
    }
    
    public String getcwd(Environment env) {
        return "/dev/null";
    }
}
