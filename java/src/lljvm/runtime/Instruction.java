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
 * This class provides methods that emulate several LLVM instructions and
 * intrinsic functions.
 * See <http://llvm.org/docs/LangRef.html> for further information.
 * 
 * @author  David Roberts
 */
public final class Instruction {
    /**
     * Thrown by the unwind instruction, caught by the invoke instruction.
     */
    @SuppressWarnings("serial")
    public static class Unwind extends RuntimeException {
        /** Pre-allocate exception for efficiency */
        public static final Unwind instance = new Unwind();
        private Unwind() {}
    }
    
    /**
     * Thrown at the beginning of any blocks of unreachable code.
     * Therefore, control flow should never reach the point where this
     * exception is thrown.
     */
    @SuppressWarnings("serial")
    public static class Unreachable extends RuntimeException {
        /** Pre-allocate exception for efficiency */
        public static final Unreachable instance = new Unreachable();
        private Unreachable() {}
    }
    
    /**
     * Prevent this class from being instantiated.
     */
    private Instruction() {}
    
    public static boolean icmp_eq(int op1, int op2) {
        return op1 == op2;
    }
    
    public static boolean icmp_eq(long op1, long op2) {
        return op1 == op2;
    }
    
    public static boolean icmp_ne(int op1, int op2) {
        return op1 != op2;
    }
    
    public static boolean icmp_ne(long op1, long op2) {
        return op1 != op2;
    }
    
    public static boolean icmp_sgt(int op1, int op2) {
        return op1 > op2;
    }
    
    public static boolean icmp_sgt(long op1, long op2) {
        return op1 > op2;
    }
    
    public static boolean icmp_sge(int op1, int op2) {
        return op1 >= op2;
    }
    
    public static boolean icmp_sge(long op1, long op2) {
        return op1 >= op2;
    }
    
    public static boolean icmp_slt(int op1, int op2) {
        return op1 < op2;
    }
    
    public static boolean icmp_slt(long op1, long op2) {
        return op1 < op2;
    }
    
    public static boolean icmp_sle(int op1, int op2) {
        return op1 <= op2;
    }
    
    public static boolean icmp_sle(long op1, long op2) {
        return op1 <= op2;
    }
    
    public static boolean icmp_ugt(int op1, int op2) {
        return ((op1^op2) >= 0) ? (op1 > op2) : (op2 > op1);
    }
    
    public static boolean icmp_ugt(long op1, long op2) {
        return ((op1^op2) >= 0) ? (op1 > op2) : (op2 > op1);
    }
    
    public static boolean icmp_uge(int op1, int op2) {
        return ((op1^op2) >= 0) ? (op1 >= op2) : (op2 >= op1);
    }
    
    public static boolean icmp_uge(long op1, long op2) {
        return ((op1^op2) >= 0) ? (op1 >= op2) : (op2 >= op1);
    }
    
    public static boolean icmp_ult(int op1, int op2) {
        return ((op1^op2) >= 0) ? (op1 < op2) : (op2 < op1);
    }
    
    public static boolean icmp_ult(long op1, long op2) {
        return ((op1^op2) >= 0) ? (op1 < op2) : (op2 < op1);
    }
    
    public static boolean icmp_ule(int op1, int op2) {
        return ((op1^op2) >= 0) ? (op1 <= op2) : (op2 <= op1);
    }
    
    public static boolean icmp_ule(long op1, long op2) {
        return ((op1^op2) >= 0) ? (op1 <= op2) : (op2 <= op1);
    }
    
    public static boolean fcmp_oeq(float op1, float op2) {
        return fcmp_ord(op1, op2) && op1 == op2;
    }
    
    public static boolean fcmp_oeq(double op1, double op2) {
        return fcmp_ord(op1, op2) && op1 == op2;
    }
    
    public static boolean fcmp_ogt(float op1, float op2) {
        return fcmp_ord(op1, op2) && op1 > op2;
    }
    
    public static boolean fcmp_ogt(double op1, double op2) {
        return fcmp_ord(op1, op2) && op1 > op2;
    }
    
    public static boolean fcmp_oge(float op1, float op2) {
        return fcmp_ord(op1, op2) && op1 >= op2;
    }
    
    public static boolean fcmp_oge(double op1, double op2) {
        return fcmp_ord(op1, op2) && op1 >= op2;
    }
    
    public static boolean fcmp_olt(float op1, float op2) {
        return fcmp_ord(op1, op2) && op1 < op2;
    }
    
    public static boolean fcmp_olt(double op1, double op2) {
        return fcmp_ord(op1, op2) && op1 < op2;
    }
    
    public static boolean fcmp_ole(float op1, float op2) {
        return fcmp_ord(op1, op2) && op1 <= op2;
    }
    
    public static boolean fcmp_ole(double op1, double op2) {
        return fcmp_ord(op1, op2) && op1 <= op2;
    }
    
    public static boolean fcmp_one(float op1, float op2) {
        return fcmp_ord(op1, op2) && op1 != op2;
    }
    
    public static boolean fcmp_one(double op1, double op2) {
        return fcmp_ord(op1, op2) && op1 != op2;
    }
    
    public static boolean fcmp_ord(float op1, float op2) {
        return !Float.isNaN(op1) && !Float.isNaN(op2);
    }
    
    public static boolean fcmp_ord(double op1, double op2) {
        return !Double.isNaN(op1) && !Double.isNaN(op2);
    }
    
    public static boolean fcmp_ueq(float op1, float op2) {
        return fcmp_uno(op1, op2) || op1 == op2;
    }
    
    public static boolean fcmp_ueq(double op1, double op2) {
        return fcmp_uno(op1, op2) || op1 == op2;
    }
    
    public static boolean fcmp_ugt(float op1, float op2) {
        return fcmp_uno(op1, op2) || op1 > op2;
    }
    
    public static boolean fcmp_ugt(double op1, double op2) {
        return fcmp_uno(op1, op2) || op1 > op2;
    }
    
    public static boolean fcmp_uge(float op1, float op2) {
        return fcmp_uno(op1, op2) || op1 >= op2;
    }
    
    public static boolean fcmp_uge(double op1, double op2) {
        return fcmp_uno(op1, op2) || op1 >= op2;
    }
    
    public static boolean fcmp_ult(float op1, float op2) {
        return fcmp_uno(op1, op2) || op1 < op2;
    }
    
    public static boolean fcmp_ult(double op1, double op2) {
        return fcmp_uno(op1, op2) || op1 < op2;
    }
    
    public static boolean fcmp_ule(float op1, float op2) {
        return fcmp_uno(op1, op2) || op1 <= op2;
    }
    
    public static boolean fcmp_ule(double op1, double op2) {
        return fcmp_uno(op1, op2) || op1 <= op2;
    }
    
    public static boolean fcmp_une(float op1, float op2) {
        return fcmp_uno(op1, op2) || op1 != op2;
    }
    
    public static boolean fcmp_une(double op1, double op2) {
        return fcmp_uno(op1, op2) || op1 != op2;
    }
    
    public static boolean fcmp_uno(float op1, float op2) {
        return Float.isNaN(op1) || Float.isNaN(op2);
    }
    
    public static boolean fcmp_uno(double op1, double op2) {
        return Double.isNaN(op1) || Double.isNaN(op2);
    }
    
    public static int zext_i32(boolean value) {
        return value ? 1 : 0;
    }
    
    public static int zext_i32(byte value) {
        return ((int)value) & 0xff;
    }
    
    public static int zext_i32(short value) {
        return ((int)value) & 0xffff;
    }
    
    public static long zext_i64(boolean value) {
        return value ? 1L : 0L;
    }
    
    public static long zext_i64(byte value) {
        return ((long)value) & 0xffL;
    }
    
    public static long zext_i64(short value) {
        return ((long)value) & 0xffffL;
    }
    
    public static long zext_i64(int value) {
        return ((long)value) & 0xffffffffL;
    }
    
    public static byte udiv(byte op1, byte op2) {
        return ((op1|op2) >= 0) ? (byte)(op1 / op2)
                                : (byte)(zext_i32(op1) / zext_i32(op2));
    }
    
    public static short udiv(short op1, short op2) {
        return ((op1|op2) >= 0) ? (short)(op1 / op2)
                                : (short)(zext_i32(op1) / zext_i32(op2));
    }
    
    public static int udiv(int op1, int op2) {
        return ((op1|op2) >= 0) ? (int)(op1 / op2)
                                : (int)(zext_i64(op1) / zext_i64(op2));
    }
    
    public static long udiv(long op1, long op2) {
        if(op1 >= 0) {
            if(op2 >= 0) // small / small
                return op1 / op2;
            else // small / big
                return 0;
        } else {
            if(op2 >= 0) { // big / small
                final long half_op1 = op1 >>> 1;
                final long half_result = half_op1 / op2;
                if(half_result == 0)
                    return 1;
                final long half_rem = half_op1 - (op2 * half_result);
                final long op1_parity = op1 & 1;
                final long rem = half_rem + half_rem + op1_parity;
                long result = half_result + half_result;
                if(rem >= op2)
                    result++;
                return result;
            } else { // big / big
                if(op1 >= op2)
                    return 1;
                else
                    return 0;
            }
        }
    }
    
    public static byte urem(byte op1, byte op2) {
        return ((op1|op2) >= 0) ? (byte)(op1 % op2)
                                : (byte)(zext_i32(op1) % zext_i32(op2));
    }
    
    public static short urem(short op1, short op2) {
        return ((op1|op2) >= 0) ? (short)(op1 % op2)
                                : (short)(zext_i32(op1) % zext_i32(op2));
    }
    
    public static int urem(int op1, int op2) {
        return ((op1|op2) >= 0) ? (int)(op1 % op2)
                                : (int)(zext_i64(op1) % zext_i64(op2));
    }
    
    public static long urem(long op1, long op2) {
        if(op1 >= 0) {
            if(op2 >= 0) // small % small
                return op1 % op2;
            else // small % big
                return op1;
        } else {
            if(op2 >= 0) { // big % small
                final long half_op1 = op1 >>> 1;
                final long half_result = half_op1 / op2;
                if(half_result == 0)
                    return op1 - op2;
                final long half_rem = half_op1 - (op2 * half_result);
                final long op1_parity = op1 & 1;
                long rem = half_rem + half_rem + op1_parity;
                if(rem >= op2)
                    rem -= op2;
                return rem;
            } else { // big % big
                if(op1 >= op2)
                    return op1 - op2;
                else
                    return op1;
            }
        }
    }
    
    public static float uitofp_f32(byte value) {
        return (float)uitofp_f64(value);
    }
    
    public static float uitofp_f32(short value) {
        return (float)uitofp_f64(value);
    }
    
    public static float uitofp_f32(int value) {
        return (float)uitofp_f64(value);
    }
    
    public static float uitofp_f32(long value) {
        return (float)uitofp_f64(value);
    }
    
    public static double uitofp_f64(byte value) {
        return (value >= 0) ? (double)value
                            : (double)value + (double)0x100L;
    }
    
    public static double uitofp_f64(short value) {
        return (value >= 0) ? (double)value
                            : (double)value + (double)0x10000L;
    }
    
    public static double uitofp_f64(int value) {
        return (value >= 0) ? (double)value
                            : (double)value + (double)0x100000000L;
    }
    
    public static double uitofp_f64(long value) {
        if(value >= 0)
            return (double)value;
        long mantissa = value >>> 11;
        int discard = (int)value & 0x7ff; // discarded 11 bits
        // round to nearest, ties to even
        if(discard > 0x400 || (discard == 0x400 && ((int)mantissa & 1) == 1))
            mantissa++;
        return (double)mantissa * (double)(1<<11);
    }
    
    public static byte fptoui_i8(float value) {
        return (byte)(int)value;
    }
    
    public static byte fptoui_i8(double value) {
        return (byte)(int)value;
    }
    
    public static short fptoui_i16(float value) {
        return (short)(int)value;
    }
    
    public static short fptoui_i16(double value) {
        return (short)(int)value;
    }
    
    public static int fptoui_i32(float value) {
        return (int)(long)value;
    }
    
    public static int fptoui_i32(double value) {
        return (int)(long)value;
    }
    
    public static long fptoui_i64(float value) {
        return fptoui_i64((double)value);
    }
    
    public static long fptoui_i64(double value) {
        return (value <= Long.MAX_VALUE)
            ? (long)value
            : Long.MIN_VALUE + (long)(value - (double)Long.MAX_VALUE - 1.0);
    }
    
    public static short bswap(short value) {
        final int b0 = (value >>> 0) & 0xff;
        final int b1 = (value >>> 8) & 0xff;
        return (short) (b0 << 8 | b1 << 0);
    }
    
    public static int bswap(int value) {
        final int b0 = (value >>> 0)  & 0xff;
        final int b1 = (value >>> 8)  & 0xff;
        final int b2 = (value >>> 16) & 0xff;
        final int b3 = (value >>> 24) & 0xff;
        return b0 << 24 | b1 << 16 | b2 << 8 | b3 << 0;
    }
    
    public static long bswap(long value) {
        final long b0 = (value >>> 0)  & 0xff;
        final long b1 = (value >>> 8)  & 0xff;
        final long b2 = (value >>> 16) & 0xff;
        final long b3 = (value >>> 24) & 0xff;
        final long b4 = (value >>> 32) & 0xff;
        final long b5 = (value >>> 40) & 0xff;
        final long b6 = (value >>> 48) & 0xff;
        final long b7 = (value >>> 56) & 0xff;
        return b0 << 56 | b1 << 48 | b2 << 40 | b3 << 32
             | b4 << 24 | b5 << 16 | b6 << 8  | b7 << 0;
    }
}
