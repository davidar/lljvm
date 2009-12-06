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

/**
 * Provides errno - the number of the last error, along with its associated
 * error value constants.
 * 
 * @author  David Roberts
 */
public final class Error {
    /** Not super-user */
    public static final int EPERM = 1;
    /** No such file or directory */
    public static final int ENOENT = 2;
    /** No such process */
    public static final int ESRCH = 3;
    /** final interrupted system call */
    public static final int EINTR = 4;
    /** I/O error */
    public static final int EIO = 5;
    /** No such device or address */
    public static final int ENXIO = 6;
    /** Arg list too long */
    public static final int E2BIG = 7;
    /** Exec format error */
    public static final int ENOEXEC = 8;
    /** Bad file number */
    public static final int EBADF = 9;
    /** No children */
    public static final int ECHILD = 10;
    /** No more processes */
    public static final int EAGAIN = 11;
    /** Not enough core */
    public static final int ENOMEM = 12;
    /** Permission denied */
    public static final int EACCES = 13;
    /** Bad address */
    public static final int EFAULT = 14;
    /** Block device required */
    public static final int ENOTBLK = 15;
    /** Mount device busy */
    public static final int EBUSY = 16;
    /** File exists */
    public static final int EEXIST = 17;
    /** Cross-device link */
    public static final int EXDEV = 18;
    /** No such device */
    public static final int ENODEV = 19;
    /** Not a directory */
    public static final int ENOTDIR = 20;
    /** Is a directory */
    public static final int EISDIR = 21;
    /** Invalid argument */
    public static final int EINVAL = 22;
    /** Too many open files in system */
    public static final int ENFILE = 23;
    /** Too many open files */
    public static final int EMFILE = 24;
    /** Not a typewriter */
    public static final int ENOTTY = 25;
    /** Text file busy */
    public static final int ETXTBSY = 26;
    /** File too large */
    public static final int EFBIG = 27;
    /** No space left on device */
    public static final int ENOSPC = 28;
    /** Illegal seek */
    public static final int ESPIPE = 29;
    /** Read only file system */
    public static final int EROFS = 30;
    /** Too many links */
    public static final int EMLINK = 31;
    /** Broken pipe */
    public static final int EPIPE = 32;
    /** Math arg out of domain of func */
    public static final int EDOM = 33;
    /** Math result not representable */
    public static final int ERANGE = 34;
    /** No message of desired type */
    public static final int ENOMSG = 35;
    /** Identifier removed */
    public static final int EIDRM = 36;
    /** Channel number out of range */
    public static final int ECHRNG = 37;
    /** Level 2 not synchronized */
    public static final int EL2NSYNC = 38;
    /** Level 3 halted */
    public static final int EL3HLT = 39;
    /** Level 3 reset */
    public static final int EL3RST = 40;
    /** Link number out of range */
    public static final int ELNRNG = 41;
    /** Protocol driver not attached */
    public static final int EUNATCH = 42;
    /** No CSI structure available */
    public static final int ENOCSI = 43;
    /** Level 2 halted */
    public static final int EL2HLT = 44;
    /** Deadlock condition */
    public static final int EDEADLK = 45;
    /** No record locks available */
    public static final int ENOLCK = 46;
    /** Invalid exchange */
    public static final int EBADE = 50;
    /** Invalid request descriptor */
    public static final int EBADR = 51;
    /** Exchange full */
    public static final int EXFULL = 52;
    /** No anode */
    public static final int ENOANO = 53;
    /** Invalid request code */
    public static final int EBADRQC = 54;
    /** Invalid slot */
    public static final int EBADSLT = 55;
    /** File locking deadlock error */
    public static final int EDEADLOCK = 56;
    /** Bad font file fmt */
    public static final int EBFONT = 57;
    /** Device not a stream */
    public static final int ENOSTR = 60;
    /** No data (for no delay io) */
    public static final int ENODATA = 61;
    /** Timer expired */
    public static final int ETIME = 62;
    /** Out of streams resources */
    public static final int ENOSR = 63;
    /** Machine is not on the network */
    public static final int ENONET = 64;
    /** Package not installed */
    public static final int ENOPKG = 65;
    /** The object is remote */
    public static final int EREMOTE = 66;
    /** The link has been severed */
    public static final int ENOLINK = 67;
    /** Advertise error */
    public static final int EADV = 68;
    /** Srmount error */
    public static final int ESRMNT = 69;
    /** Communication error on send */
    public static final int ECOMM = 70;
    /** Protocol error */
    public static final int EPROTO = 71;
    /** Multihop attempted */
    public static final int EMULTIHOP = 74;
    /** Inode is remote (not really error) */
    public static final int ELBIN = 75;
    /** Cross mount pofinal int (not really error) */
    public static final int EDOTDOT = 76;
    /** Trying to read unreadable message */
    public static final int EBADMSG = 77;
    /** Inappropriate file type or format */
    public static final int EFTYPE = 79;
    /** Given log. name not unique */
    public static final int ENOTUNIQ = 80;
    /** f.d. invalid for this operation */
    public static final int EBADFD = 81;
    /** Remote address changed */
    public static final int EREMCHG = 82;
    /** Can't access a needed shared lib */
    public static final int ELIBACC = 83;
    /** Accessing a corrupted shared lib */
    public static final int ELIBBAD = 84;
    /** .lib section in a.out corrupted */
    public static final int ELIBSCN = 85;
    /** Attempting to link in too many libs */
    public static final int ELIBMAX = 86;
    /** Attempting to exec a shared library */
    public static final int ELIBEXEC = 87;
    /** Function not implemented */
    public static final int ENOSYS = 88;
    /** No more files */
    public static final int ENMFILE = 89;
    /** Directory not empty */
    public static final int ENOTEMPTY = 90;
    /** File or path name too long */
    public static final int ENAMETOOLONG = 91;
    /** Too many symbolic links */
    public static final int ELOOP = 92;
    /** Operation not supported on transport endpofinal int */
    public static final int EOPNOTSUPP = 95;
    /** Protocol family not supported */
    public static final int EPFNOSUPPORT = 96;
    /** Connection reset by peer */
    public static final int ECONNRESET = 104;
    /** No buffer space available */
    public static final int ENOBUFS = 105;
    /** Address family not supported by protocol family */
    public static final int EAFNOSUPPORT = 106;
    /** Protocol wrong type for socket */
    public static final int EPROTOTYPE = 107;
    /** Socket operation on non-socket */
    public static final int ENOTSOCK = 108;
    /** Protocol not available */
    public static final int ENOPROTOOPT = 109;
    /** Can't send after socket shutdown */
    public static final int ESHUTDOWN = 110;
    /** Connection refused */
    public static final int ECONNREFUSED = 111;
    /** Address already in use */
    public static final int EADDRINUSE = 112;
    /** Connection aborted */
    public static final int ECONNABORTED = 113;
    /** Network is unreachable */
    public static final int ENETUNREACH = 114;
    /** Network final interface is not configured */
    public static final int ENETDOWN = 115;
    /** Connection timed out */
    public static final int ETIMEDOUT = 116;
    /** Host is down */
    public static final int EHOSTDOWN = 117;
    /** Host is unreachable */
    public static final int EHOSTUNREACH = 118;
    /** Connection already in progress */
    public static final int EINPROGRESS = 119;
    /** Socket already connected */
    public static final int EALREADY = 120;
    /** Destination address required */
    public static final int EDESTADDRREQ = 121;
    /** Message too long */
    public static final int EMSGSIZE = 122;
    /** Unknown protocol */
    public static final int EPROTONOSUPPORT = 123;
    /** Socket type not supported */
    public static final int ESOCKTNOSUPPORT = 124;
    /** Address not available */
    public static final int EADDRNOTAVAIL = 125;
    /** Connection aborted by network */
    public static final int ENETRESET = 126;
    /** Socket is already connected */
    public static final int EISCONN = 127;
    /** Socket is not connected */
    public static final int ENOTCONN = 128;
    /** Too many references: cannot splice */
    public static final int ETOOMANYREFS = 129;
    /** Too many processes */
    public static final int EPROCLIM = 130;
    /** Too many users */
    public static final int EUSERS = 131;
    /** Disk quota exceeded */
    public static final int EDQUOT = 132;
    /** Stale file handle */
    public static final int ESTALE = 133;
    /** Not supported */
    public static final int ENOTSUP = 134;
    /** No medium (in tape drive) */
    public static final int ENOMEDIUM = 135;
    /** No such host or network path */
    public static final int ENOSHARE = 136;
    /** Filename exists with different case */
    public static final int ECASECLASH = 137;
    /** Illegal byte sequence */
    public static final int EILSEQ = 138;
    /** Value too large for defined data type */
    public static final int EOVERFLOW = 139;
    /** Operation canceled */
    public static final int ECANCELED = 140;
    /** State not recoverable */
    public static final int ENOTRECOVERABLE = 141;
    /** Previous owner died */
    public static final int EOWNERDEAD = 142;
    
    /** Pointer to errno */
    public static int errno = Memory.allocateData(4);
    
    /**
     * Prevent this class from being instantiated.
     */
    private Error() {}
    
    /**
     * Returns the value of errno.
     * 
     * @return  the value of errno
     */
    public static int errno() {
        return Memory.load_i32(errno);
    }
    
    /**
     * Sets the value of errno.
     * 
     * @param value  the new value of errno
     * @return       -1
     */
    public static int errno(int value) {
        Memory.store(errno, value);
        return -1;
    }
}
