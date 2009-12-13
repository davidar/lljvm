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
    /**
     * Prevent this class from being instantiated.
     */
    private Math() {}
    
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
