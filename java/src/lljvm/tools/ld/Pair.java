/*
* Copyright (c) 2011 Joshua Arnold
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
package lljvm.tools.ld;

final class Pair<F,S> {
    private static final int HASH_PRIME = 23887;
    final F first;
    final S second;
    Pair(F first, S second) {
        super();
        this.first = first;
        this.second = second;
    }
    
    private static int hash(Object x) {
        return x !=null ? x.hashCode() : 0;
    }
    private static boolean eq(Object a, Object b) {
        return a==b ? true : (a==null ? false : a.equals(b));
    }
    
    @Override
    public int hashCode() {
        return HASH_PRIME * hash(first) + hash(second);
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair<?,?>))
            return false;
        Pair<?,?> other = (Pair<?,?>)obj;
        return eq(first,other.first) && eq(second,other.second);
    }
    
    @Override
    public String toString() {
        return "("+first+","+second+")";
    }
    
    static <F,S> Pair<F,S> of(F f, S s) {
        return new Pair<F,S>(f,s);
    }
}
