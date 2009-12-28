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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import lljvm.util.ReflectionUtils;

/**
 * Virtual memory, with methods for storing/loading values to/from
 * specified addresses.
 * 
 * @author  David Roberts
 */
public final class Memory {
    private static final int ALIGNMENT = 8; // 8-byte alignment
    private static final int MEM_SIZE = 1<<30; // 1 GiB of virtual memory
    private static final int DATA_SIZE = 1<<20; // 1 MiB Data+BSS
    private static final int STACK_SIZE = 1<<20; // 1 MiB stack
    
    // 64 KiB pages
    private static final int PAGE_SHIFT = 16;
    private static final int PAGE_SIZE = 1<<PAGE_SHIFT;
    
    private static final ByteOrder ENDIANNESS = ByteOrder.LITTLE_ENDIAN;
    
    /** Array of pages */
    private static final ByteBuffer[] pages =
        new ByteBuffer[MEM_SIZE>>>PAGE_SHIFT];
    /** Current end of Data+BSS */
    private static int dataEnd = 0;
    /** Current end of the heap */
    private static int heapEnd = DATA_SIZE;
    /** Current frame pointer */
    private static int framePointer = MEM_SIZE;
    /** Current stack pointer */
    private static int stackPointer = framePointer;
    /** Current number of frames on the stack */
    private static int stackDepth = 0;
    
    /** The null pointer */
    public static final int NULL = allocateData();
    
    static {
        final int DATA_BOTTOM = 0>>>PAGE_SHIFT;
        final int DATA_END = (DATA_SIZE - 1)>>>PAGE_SHIFT;
        for(int i = DATA_BOTTOM; i <= DATA_END; i++)
            pages[i] = createPage();
        final int STACK_BOTTOM = (MEM_SIZE - STACK_SIZE)>>>PAGE_SHIFT;
        final int STACK_END = (MEM_SIZE - 1)>>>PAGE_SHIFT;
        for(int i = STACK_BOTTOM; i <= STACK_END; i++)
            pages[i] = createPage();
    }
    
    /**
     * Thrown if an application tries to access an invalid memory address, or
     * tries to write to a read-only location.
     */
    @SuppressWarnings("serial")
    public static class SegmentationFault extends IllegalArgumentException {
        public SegmentationFault(int addr) {
            super("Address = "+addr+" (0x"+Integer.toHexString(addr)+")");
        }
    }

    /**
     * Prevent this class from being instantiated.
     */
    private Memory() {}
    
    /**
     * Create a new page.
     * @return  the new page
     */
    private static ByteBuffer createPage() {
        return ByteBuffer.allocateDirect(PAGE_SIZE).order(ENDIANNESS);
    }
    
    /**
     * Return the page of the given virtual memory address
     * 
     * @param addr  the virtual memory address
     * @return      the page of the given virtual memory address
     */
    private static ByteBuffer getPage(int addr) {
        try {
            return pages[addr>>>PAGE_SHIFT];
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new SegmentationFault(addr);
        }
    }

    /**
     * Return the offset within the page of the given virtual memory address
     * 
     * @param addr  the virtual memory address
     * @return      the offset of the given virtual memory address
     */
    private static int getOffset(int addr) {
        return addr & (PAGE_SIZE - 1);
    }
    
    /**
     * Returns the least address greater than offset which is a multiple of
     * align.
     * 
     * @param offset  the offset to align
     * @param align   the required alignment. Must be a power of two.
     * @return        the aligned offset
     */
    private static int alignOffsetUp(int offset, int align) {
        return ((offset-1) & ~(align-1)) + align;
    }

    /**
     * Returns the greatest address less than offset which is a multiple of
     * align.
     * 
     * @param offset  the offset to align
     * @param align   the required alignment. Must be a power of two.
     * @return        the aligned offset
     */
    private static int alignOffsetDown(int offset, int align) {
        return offset & ~(align-1);
    }
    
    /**
     * Create a new stack frame, storing the current frame pointer.
     */
    public static void createStackFrame() {
        final int prevFramePointer = framePointer;
        framePointer = stackPointer;
        storeStack(prevFramePointer);
        stackDepth++;
    }
    
    /**
     * Destroy the current stack frame, restoring the previous frame pointer.
     */
    public static void destroyStackFrame() {
        stackPointer = framePointer;
        framePointer = load_i32(stackPointer - ALIGNMENT);
        stackDepth--;
    }
    
    /**
     * Destroy the top n stack frames.
     * 
     * @param n  the number of stack frames to destroy
     */
    public static void destroyStackFrames(int n) {
        for(int i = 0; i < n; i++)
            destroyStackFrame();
    }
    
    /**
     * Return the number of stack frames currently on the stack.
     * 
     * @return  the number of stack frames currently on the stack
     */
    public static int getStackDepth() {
        return stackDepth;
    }
    
    /**
     * Allocate a block of the given size within the data segment.
     * 
     * @param size  the size of the block to allocate
     * @return      a pointer to the allocated block
     */
    public static int allocateData(int size) {
        final int addr = dataEnd;
        dataEnd = alignOffsetUp(dataEnd + size, ALIGNMENT);
        return addr;
    }
    
    /**
     * Allocate one byte within the data segment.
     * 
     * @return  a pointer to the allocated byte
     */
    public static int allocateData() {
        return allocateData(1);
    }
    
    /**
     * Allocate a block of the given size within the stack.
     * 
     * @param size  the size of the block to allocate
     * @return      a pointer to the allocated block
     */
    public static int allocateStack(int size) {
        stackPointer = alignOffsetDown(stackPointer - size, ALIGNMENT);
        return stackPointer;
    }
    
    /**
     * Allocate one byte within the stack.
     * 
     * @return  a pointer to the allocated byte
     */
    public static int allocateStack() {
        return allocateStack(1);
    }
    
    /**
     * Increase the size of the heap by the specified amount.
     * 
     * @param increment  the amount to increment the heap size
     * @return           a pointer to the previous end of the heap on success,
     *                   -1 on error
     */
    public static int sbrk(int increment) {
        final int prevHeapEnd = heapEnd;
        if(heapEnd + increment > MEM_SIZE - STACK_SIZE
        || heapEnd + increment < DATA_SIZE)
            return Error.errno(Error.ENOMEM);
        heapEnd += increment;
        final int HEAP_BOTTOM = prevHeapEnd>>>PAGE_SHIFT;
        final int HEAP_END = (heapEnd - 1)>>>PAGE_SHIFT;
        for(int i = HEAP_BOTTOM; i <= HEAP_END; i++)
            if(pages[i] == null)
                pages[i] = createPage();
        // TODO: destroy pages if increment < 0
        return prevHeapEnd;
    }
    
    /**
     * Store a boolean value at the given address.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     */
    public static void store(int addr, boolean value) {
        try {
            getPage(addr).put(getOffset(addr), (byte) (value ? 1 : 0));
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Store a byte at the given address.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     */
    public static void store(int addr, byte value) {
        try {
            getPage(addr).put(getOffset(addr), value);
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Store a 16-bit integer at the given address.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     */
    public static void store(int addr, short value) {
        try {
            getPage(addr).putShort(getOffset(addr), value);
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Store a 32-bit integer at the given address.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     */
    public static void store(int addr, int value) {
        try {
            getPage(addr).putInt(getOffset(addr), value);
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Store a 64-bit integer at the given address.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     */
    public static void store(int addr, long value) {
        try {
            getPage(addr).putLong(getOffset(addr), value);
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Store a single precision floating point number at the given address.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     */
    public static void store(int addr, float value) {
        try {
            getPage(addr).putFloat(getOffset(addr), value);
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Store a double precision floating point number at the given address.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     */
    public static void store(int addr, double value) {
        try {
            getPage(addr).putDouble(getOffset(addr), value);
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Store an array of bytes at the given address.
     * 
     * @param addr   the address at which to store the bytes
     * @param bytes  the bytes to be stored
     */
    public static void store(int addr, byte[] bytes) {
        // TODO: make more efficient by using put(byte[])
        for(int i = 0; i < bytes.length; i++)
            store(addr + i, bytes[i]);
    }
    
    /**
     * Store a string at the given address.
     * 
     * @param addr    the address at which to store the string
     * @param string  the string to be stored
     */
    public static void store(int addr, String string) {
        final byte[] bytes = string.getBytes();
        store(addr, bytes);
        Memory.store(addr + bytes.length, (byte) 0);
    }
    
    /**
     * Store a string at the given address, unless the string would occupy more
     * than size bytes (including the null terminator).
     * 
     * @param addr    the address at which to store the string
     * @param string  the string to be stored
     * @param size    the maximum size of the string
     * @return        addr on success, NULL on error
     */
    public static int store(int addr, String string, int size) {
        final byte[] bytes = string.getBytes();
        if(bytes.length + 1 > size) {
            Error.errno(Error.ERANGE);
            return NULL;
        }
        store(addr, bytes);
        Memory.store(addr + bytes.length, (byte) 0);
        return addr;
    }
    
    /**
     * Store a boolean value in the data segment, returning a pointer to the
     * value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeData(boolean value) {
        final int addr = allocateData(1);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a byte in the data segment, returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeData(byte value) {
        final int addr = allocateData(1);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a 16-bit integer in the data segment, returning a pointer to the
     * value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeData(short value) {
        final int addr = allocateData(2);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a 32-bit integer in the data segment, returning a pointer to the
     * value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeData(int value) {
        final int addr = allocateData(4);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a 64-bit integer in the data segment, returning a pointer to the
     * value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeData(long value) {
        final int addr = allocateData(8);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a single precision floating point number in the data segment,
     * returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeData(float value) {
        final int addr = allocateData(4);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a double precision floating point number in the data segment,
     * returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeData(double value) {
        final int addr = allocateData(8);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store an array of bytes in the data segment, returning a pointer to the
     * bytes.
     * 
     * @param bytes  the bytes to be stored
     * @return       a pointer to the bytes
     */
    public static int storeData(byte[] bytes) {
        final int addr = allocateData(bytes.length);
        store(addr, bytes);
        return addr;
    }
    
    /**
     * Store a string in the data segment, returning a pointer to the string.
     * 
     * @param string  the string to be stored
     * @return        a pointer to the string
     */
    public static int storeData(String string) {
        final byte[] bytes = string.getBytes();
        final int addr = allocateData(bytes.length+1);
        store(addr, bytes);
        Memory.store(addr + bytes.length, (byte) 0);
        return addr;
    }
    
    /**
     * Store a boolean value in the stack, returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeStack(boolean value) {
        final int addr = allocateStack(1);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a byte in the stack, returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeStack(byte value) {
        final int addr = allocateStack(1);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a 16-bit integer in the stack, returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeStack(short value) {
        final int addr = allocateStack(2);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a 32-bit integer in the stack, returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeStack(int value) {
        final int addr = allocateStack(4);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a 64-bit integer in the stack, returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeStack(long value) {
        final int addr = allocateStack(8);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a single precision floating point number in the stack,
     * returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeStack(float value) {
        final int addr = allocateStack(4);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store a double precision floating point number in the stack,
     * returning a pointer to the value.
     * 
     * @param value  the value to be stored
     * @return       a pointer to the value
     */
    public static int storeStack(double value) {
        final int addr = allocateStack(8);
        store(addr, value);
        return addr;
    }
    
    /**
     * Store an array of bytes in the stack, returning a pointer to the bytes.
     * 
     * @param bytes  the bytes to be stored
     * @return       a pointer to the bytes
     */
    public static int storeStack(byte[] bytes) {
        final int addr = allocateStack(bytes.length);
        store(addr, bytes);
        return addr;
    }
    
    /**
     * Store an array of strings in the stack, terminated by a null pointer.
     * 
     * @param strings  the array of strings to be stored
     * @return         a pointer to the array
     */
    public static int storeStack(String[] strings) {
        final int addr = allocateStack(strings.length * 4 + 4);
        for(int i = 0; i < strings.length; i++)
            store(addr + i * 4, storeStack(strings[i]));
        store(addr + strings.length * 4, NULL);
        return addr;
    }
    
    /**
     * Store a string in the stack, returning a pointer to the string.
     * 
     * @param string  the string to be stored
     * @return        a pointer to the string
     */
    public static int storeStack(String string) {
        final byte[] bytes = string.getBytes();
        final int addr = allocateStack(bytes.length+1);
        store(addr, bytes);
        Memory.store(addr + bytes.length, (byte) 0);
        return addr;
    }
    
    /**
     * Load a boolean value from the given address.
     * 
     * @param addr  the address from which to load the value
     * @return      the value at the given address
     */
    public static boolean load_i1(int addr) {
        try {
            return getPage(addr).get(getOffset(addr)) != 0;
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Load a byte from the given address.
     * 
     * @param addr  the address from which to load the value
     * @return      the value at the given address
     */
    public static byte load_i8(int addr) {
        try {
            return getPage(addr).get(getOffset(addr));
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Load a 16-bit integer from the given address.
     * 
     * @param addr  the address from which to load the value
     * @return      the value at the given address
     */
    public static short load_i16(int addr) {
        try {
            return getPage(addr).getShort(getOffset(addr));
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Load a 32-bit integer from the given address.
     * 
     * @param addr  the address from which to load the value
     * @return      the value at the given address
     */
    public static int load_i32(int addr) {
        try {
            return getPage(addr).getInt(getOffset(addr));
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Load a 64-bit integer from the given address.
     * 
     * @param addr  the address from which to load the value
     * @return      the value at the given address
     */
    public static long load_i64(int addr) {
        try {
            return getPage(addr).getLong(getOffset(addr));
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Load a single precision floating point number from the given address.
     * 
     * @param addr  the address from which to load the value
     * @return      the value at the given address
     */
    public static float load_f32(int addr) {
        try {
            return getPage(addr).getFloat(getOffset(addr));
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Load a double precision floating point number from the given address.
     * 
     * @param addr  the address from which to load the value
     * @return      the value at the given address
     */
    public static double load_f64(int addr) {
        try {
            return getPage(addr).getDouble(getOffset(addr));
        } catch(NullPointerException e) {
            throw new SegmentationFault(addr);
        }
    }
    
    /**
     * Load a string from the given address.
     * 
     * @param addr  the address from which to load the string
     * @return      the string at the given address
     */
    public static String load_string(int addr) {
        byte[] bytes = new byte[16];
        int i = 0;
        while((bytes[i++] = load_i8(addr++)) != 0)
            if(i >= bytes.length) bytes = Arrays.copyOf(bytes, i*2);
        return new String(Arrays.copyOf(bytes, i));
    }
    
    /**
     * Load a value of the given type from the given address.
     * 
     * @param addr  the address from which to load the value
     * @param type  the type of value to load. Must be a primitive type other
     *              than char.
     * @return      the value at the given address
     */
    public static Object load(int addr, Class<?> type) {
        if(type == boolean.class) return load_i1(addr);
        if(type == byte.class)    return load_i8(addr);
        if(type == short.class)   return load_i16(addr);
        if(type == int.class)     return load_i32(addr);
        if(type == long.class)    return load_i64(addr);
        if(type == float.class)   return load_f32(addr);
        if(type == double.class)  return load_f64(addr);
        throw new IllegalArgumentException("Unrecognised type");
    }
    
    /**
     * Store a boolean value at the given address, inserting any required
     * padding before the value, returning the first address following the
     * value.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     * @return       the first address following the value
     */
    public static int pack(int addr, boolean value) {
        addr = alignOffsetUp(addr, 1);
        store(addr, value);
        return addr + 1;
    }
    
    /**
     * Store a byte at the given address, inserting any required padding before
     * the value, returning the first address following the value.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     * @return       the first address following the value
     */
    public static int pack(int addr, byte value) {
        addr = alignOffsetUp(addr, 1);
        store(addr, value);
        return addr + 1;
    }
    
    /**
     * Store a 16-bit integer at the given address, inserting any required
     * padding before the value, returning the first address following the
     * value.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     * @return       the first address following the value
     */
    public static int pack(int addr, short value) {
        addr = alignOffsetUp(addr, 2);
        store(addr, value);
        return addr + 2;
    }
    
    /**
     * Store a 32-bit integer at the given address, inserting any required
     * padding before the value, returning the first address following the
     * value.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     * @return       the first address following the value
     */
    public static int pack(int addr, int value) {
        addr = alignOffsetUp(addr, 4);
        store(addr, value);
        return addr + 4;
    }
    
    /**
     * Store a 64-bit integer at the given address, inserting any required
     * padding before the value, returning the first address following the
     * value.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     * @return       the first address following the value
     */
    public static int pack(int addr, long value) {
        addr = alignOffsetUp(addr, 8);
        store(addr, value);
        return addr + 8;
    }
    
    /**
     * Store a single precision floating point number at the given address,
     * inserting any required padding before the value, returning the first
     * address following the value.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     * @return       the first address following the value
     */
    public static int pack(int addr, float value) {
        addr = alignOffsetUp(addr, 4);
        store(addr, value);
        return addr + 4;
    }
    
    /**
     * Store a double precision floating point number at the given address,
     * inserting any required padding before the value, returning the first
     * address following the value.
     * 
     * @param addr   the address at which to store the value
     * @param value  the value to be stored
     * @return       the first address following the value
     */
    public static int pack(int addr, double value) {
        addr = alignOffsetUp(addr, 8);
        store(addr, value);
        return addr + 8;
    }
    
    /**
     * Store a string at the given address, returning the first address
     * following the null terminator.
     * 
     * @param addr    the address at which to store the string
     * @param string  the string to be stored
     * @return        the first address following the null terminator
     */
    public static int pack(int addr, String string) {
        final byte[] bytes = string.getBytes();
        store(addr, bytes);
        Memory.store(addr + bytes.length, (byte) 0);
        return addr + bytes.length + 1;
    }
    
    /**
     * Store an array of chars at the given address, treating it as an array of
     * bytes i.e. each char is cast to a byte before being stored.
     * 
     * @param addr   the address at which to store the array
     * @param chars  the array of chars
     * @return       the first address following the stored array
     */
    public static int pack(int addr, char[] chars) {
        for(int i = 0; i < chars.length; i++)
            Memory.store(addr + i, (byte) chars[i]);
        return addr + chars.length;
    }
    
    /**
     * Unpack a naturally-aligned value of the given size from the given
     * address. The given address is updated to point to the first address
     * following the value.
     * 
     * @param addrp  a pointer to the address
     * @param size   the size of the value in bytes. Must be a power of 2.
     * @return       the address of the first naturally-aligned value of the
     *               given size following the given address
     */
    public static int unpack(int addrp, int size) {
        int addr = Memory.load_i32(addrp);
        addr = alignOffsetUp(addr, size);
        Memory.store(addrp, addr + size);
        return addr;
    }
    
    /**
     * Unpack a packed list of values from the given address, according to
     * the given list of types.
     * 
     * @param addr   the address from which to load the values
     * @param types  the array of types. All elements must be primitive types
     *               other than char.
     * @return       an array of unpacked values
     */
    public static Object[] unpack(int addr, Class<?>[] types) {
        Object[] values = new Object[types.length];
        for(int i = 0; i < types.length; i++) {
            final Class<?> type = types[i];
            final int size = ReflectionUtils.sizeOf(type);
            addr = alignOffsetUp(addr, size);
            values[i] = load(addr, type);
            addr += size;
        }
        return values;
    }
    
    /**
     * Copy len bytes from memory area src to memory area dest. The memory
     * areas should not overlap.
     * 
     * @param dest   the destination memory area
     * @param src    the source memory area
     * @param len    the number of bytes to copy
     * @param align  the alignment of the source and destination pointers,
     *               unless align is equal to 0 or 1
     */
    public static void memcpy(int dest, int src, int len, int align) {
        // TODO: make more efficient by using put(ByteBuffer)
        for(int i = 0; i < len; i++)
            store(dest + i, load_i8(src + i));
    }
    
    /**
     * Copy len bytes from memory area src to memory area dest. The memory
     * areas should not overlap.
     * 
     * @param dest   the destination memory area
     * @param src    the source memory area
     * @param len    the number of bytes to copy
     * @param align  the alignment of the source and destination pointers,
     *               unless align is equal to 0 or 1
     */
    public static void memcpy(int dest, int src, long len, int align) {
        memcpy(dest, src, (int) len, align);
    }
    
    /**
     * Copy len bytes from memory area src to memory area dest. The memory
     * areas may overlap.
     * 
     * @param dest   the destination memory area
     * @param src    the source memory area
     * @param len    the number of bytes to copy
     * @param align  the alignment of the source and destination pointers,
     *               unless align is equal to 0 or 1
     */
    public static void memmove(int dest, int src, int len, int align) {
        // TODO: make more efficient by using put(ByteBuffer)
        if(dest < src)
            for(int i = 0; i < len; i++)
                store(dest + i, load_i8(src + i));
        else
            for(int i = len - 1; i >= 0; i--)
                store(dest + i, load_i8(src + i));
    }
    
    /**
     * Copy len bytes from memory area src to memory area dest. The memory
     * areas may overlap.
     * 
     * @param dest   the destination memory area
     * @param src    the source memory area
     * @param len    the number of bytes to copy
     * @param align  the alignment of the source and destination pointers,
     *               unless align is equal to 0 or 1
     */
    public static void memmove(int dest, int src, long len, int align) {
        memmove(dest, src, (int) len, align);
    }
    
    /**
     * Fill the first len bytes of memory area dest with the constant byte val.
     * 
     * @param dest   the destination memory area
     * @param val    the constant byte fill value
     * @param len    the number of bytes to set
     * @param align  the alignment of the source and destination pointers,
     *               unless align is equal to 0 or 1
     */
    public static void memset(int dest, byte val, int len, int align) {
        // TODO: make more efficient by setting larger blocks at a time
        for(int i = dest; i < dest + len; i++)
            store(i, val);
    }
    
    /**
     * Fill the first len bytes of memory area dest with the constant byte val.
     * 
     * @param dest   the destination memory area
     * @param val    the constant byte fill value
     * @param len    the number of bytes to set
     * @param align  the alignment of the source and destination pointers,
     *               unless align is equal to 0 or 1
     */
    public static void memset(int dest, byte val, long len, int align) {
        memset(dest, val, (int) len, align);
    }
    
    /**
     * Fill the first len bytes of memory area dest with 0.
     * 
     * @param dest  the destination memory area
     * @param len   the number of bytes to set
     * @return      the address of the first byte following the block
     */
    public static int zero(int dest, int len) {
        memset(dest, (byte) 0, len, 1);
        return dest + len;
    }
}
