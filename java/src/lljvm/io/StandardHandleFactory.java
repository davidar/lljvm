package lljvm.io;

public interface StandardHandleFactory {
    FileHandle createStdin();
    FileHandle createStdout();
    FileHandle createStderr();
}
