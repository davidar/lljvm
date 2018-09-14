package lljvm.io;

public class DefaultStandardHandleFactory extends StreamStandardHandleFactory {
    public DefaultStandardHandleFactory() {
        super(System.in,System.out,System.err);
    }
}
