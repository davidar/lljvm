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
 * Provides common math functions either not provided by Java, or provided
 * under a different name to that of libm.
 * 
 * @author  David Roberts
 */
public final class Math {
    /** Not a Number */
    public static final int FP_NAN = 0;
    /** Positive/negative infinity */
    public static final int FP_INFINITE = 1;
    /** Zero */
    public static final int FP_ZERO = 2;
    /** Too small to be represented in normalised format */
    public static final int FP_SUBNORMAL = 3;
    /** Not NaN, infinite, zero, or sub-normal */
    public static final int FP_NORMAL = 4;
    
    /**
     * Prevent this class from being instantiated.
     */
    private Math() {}
    
    /**
     * Inverse hyperbolic sine function.
     * 
     * @param x  the value whose inverse hyperbolic sine is to be returned
     * @return   the value whose hyperbolic sine is x
     */
    public static double asinh(double x) {
        return java.lang.Math.log(x + java.lang.Math.sqrt(x * x + 1));
    }
    
    /**
     * Inverse hyperbolic cosine function.
     * 
     * @param x  the value whose inverse hyperbolic cosine is to be returned
     * @return   the value whose hyperbolic cosine is x
     */
    public static double acosh(double x) {
        return java.lang.Math.log(
                x + java.lang.Math.sqrt(x - 1) * java.lang.Math.sqrt(x + 1));
    }
    
    /**
     * Inverse hyperbolic tangent function.
     * 
     * @param x  the value whose inverse hyperbolic tangent is to be returned
     * @return   the value whose hyperbolic tangent is x
     */
    public static double atanh(double x) {
        return java.lang.Math.log((1 + x) / (1 - x)) / 2;
    }
    
    /**
     * Base-2 exponential function.
     * 
     * @param x  the exponent
     * @return   2 raised to the power of x
     */
    public static double exp2(double x) {
        return java.lang.Math.pow(2, x);
    }
    
    /**
     * Return the absolute value of the double value.
     * 
     * @param x  the double value
     * @return   the absolute value of the double value
     */
    public static double fabs(double x) {
        return java.lang.Math.abs(x);
    }
    
    /**
     * Return the absolute value of the float value.
     * 
     * @param x  the float value
     * @return   the absolute value of the float value
     */
    public static float fabsf(float x) {
        return java.lang.Math.abs(x);
    }
    
    /**
     * Return the floating-point remainder of dividing x by y, rounded
     * towards zero to an integer.
     * 
     * @param x  the dividend
     * @param y  the divisor
     * @return   the remainder of the division
     */
    public static double fmod(double x, double y) {
        return x % y;
    }
    
    /**
     * Return the floating-point remainder of dividing x by y, rounded
     * towards zero to an integer.
     * 
     * @param x  the dividend
     * @param y  the divisor
     * @return   the remainder of the division
     */
    public static float fmodf(float x, float y) {
        return x % y;
    }
    
    /**
     * Classify the given floating point number.
     * 
     * @param x  the floating point number
     * @return   the integer representing the type of x:
     *           one of FP_NAN, FP_INFINITE, FP_ZERO, FP_SUBNORMAL, FP_NORMAL
     */
    public static int fpclassify(double x) {
        if(Double.isNaN(x))
            return FP_NAN;
        if(Double.isInfinite(x))
            return FP_INFINITE;
        if(x == 0.0)
            return FP_ZERO;
        if(java.lang.Math.abs(x) < Double.MIN_NORMAL)
            return FP_SUBNORMAL;
        return FP_NORMAL;
    }
    
    /**
     * Split the given number into a normalised fraction and an exponent.
     * 
     * @param x    the number to split
     * @param exp  where to store the exponent
     * @return     the normalised fraction
     */
    public static double frexp(double x, int exp) {
        if(x == 0.0)
            Memory.store(exp, 0);
        if(Double.isNaN(x) || Double.isInfinite(x) || x == 0.0)
            return x;
        long bits = Double.doubleToRawLongBits(x);
        Memory.store(exp, (int) ((bits & 0x7ff0000000000000L) >>> 52) - 1022);
        return Double.longBitsToDouble(
                (bits & 0x800fffffffffffffL) | 0x3fe0000000000000L);
    }
    
    /**
     * Split the given number into a normalised fraction and an exponent.
     * 
     * @param x    the number to split
     * @param exp  where to store the exponent
     * @return     the normalised fraction
     */
    public static float frexpf(float x, int exp) {
        if(x == 0.0)
            Memory.store(exp, 0);
        if(Float.isNaN(x) || Float.isInfinite(x) || x == 0.0)
            return x;
        int bits = Float.floatToRawIntBits(x);
        Memory.store(exp, ((bits & 0x7f800000) >>> 23) - 126);
        return Float.intBitsToFloat((bits & 0x7fffff) | 0x3f000000);
    }
    
    /**
     * Test whether the given value is infinite.
     * 
     * @param x  the value to test
     * @return   1 if x is +infinity, -1 if x is -infinity, 0 otherwise
     */
    public static int isinf(double x) {
        if(Double.isInfinite(x))
            return x == Double.POSITIVE_INFINITY ? 1 : -1;
        return 0;
    }
    
    /**
     * Test whether the given value is Not a Number.
     * 
     * @param x  the value to test
     * @return   1 if x is NaN, 0 otherwise
     */
    public static int isnan(double x) {
        return Double.isNaN(x) ? 1 : 0;
    }
    
    /**
     * Break the given number into an integral part and a fractional part.
     * 
     * @param x     the number to break
     * @param iptr  where to store the integral part
     * @return      the fractional part
     */
    public static double modf(double x, int iptr) {
        double i = (double) (int) x;
        Memory.store(iptr, i);
        return x - i;
    }
    
    /**
     * Break the given number into an integral part and a fractional part.
     * 
     * @param x     the number to break
     * @param iptr  where to store the integral part
     * @return      the fractional part
     */
    public static float modff(float x, int iptr) {
        float i = (float) (int) x;
        Memory.store(iptr, i);
        return x - i;
    }
    
    /**
     * Return the floating-point remainder of dividing x by y, rounded
     * towards the nearest integer, ties to even.
     * 
     * @param x  the dividend
     * @param y  the divisor
     * @return   the remainder of the division
     */
    public static double remainder(double x, double y) {
        return java.lang.Math.IEEEremainder(x, y);
    }
    
    /**
     * Return the floating-point remainder of dividing x by y, rounded
     * towards the nearest integer, ties to even.
     * 
     * @param x  the dividend
     * @param y  the divisor
     * @return   the remainder of the division
     */
    public static float remainderf(float x, float y) {
        return (float) remainder(x, y);
    }
}
