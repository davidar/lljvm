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

import java.util.ArrayList;


/**
 * Implements a mapping between line numbers and elements of type {@code T}, optimized for use by the linker.
 * @author Joshua Arnold
 *
 * @param <T> the type of element contained in the table.
 */
final class LineTable<T> {
    private static final int PAGE_BITS = 8;
    private static final int PAGE_SIZE = 1 << PAGE_BITS;

    private ArrayList<int[]> lineNums = new ArrayList<int[]>();
    private ArrayList<Object[]> vals = new ArrayList<Object[]>();

    private int nextAddMin = Integer.MIN_VALUE;
    private int findHint = 0;
    private int size;

    public LineTable() {
    }

    public void add(int lineNum, T val) {
        if (lineNum < nextAddMin)
            throw new IllegalStateException(String.format(
                    "line numbers must be ascending; minLine=%d,curLine=%d", nextAddMin, lineNum));
        nextAddMin = lineNum + 1;
        if (nextAddMin < lineNum)
            throw new IllegalStateException("overflow");

        int pageIndx = size >> PAGE_BITS;
        int pageOffset = size & (PAGE_SIZE - 1);
        if (pageIndx >= lineNums.size()) {
            lineNums.add(new int[PAGE_SIZE]);
            vals.add(new Object[PAGE_SIZE]);
        }
        lineNums.get(pageIndx)[pageOffset] = lineNum;
        vals.get(pageIndx)[pageOffset] = val;
        size++;
    }
    
    public int size() {
        return size;
    }

    public T get(int lineNum) {
        int index;
        //Guess at sequential access, but fall back to binary search
        if (findHint<size && lineNumAt(findHint) == lineNum) {
            index = findHint;
        } else {
            index = findLineNum(lineNum);
            if (index < 0)
                return null;
        }
        findHint = index+1;
        return valueAt(index);
    }
    
    public boolean hasEntryFor(int lineNum) {
        return findLineNum(lineNum) >= 0;
    }
    
    public T change(int lineNum, T newVal) {
        int index;
        //Heuristically, most changes happen to recent line numbers. 
        if (size>0 && lineNumAt(size-1)==lineNum) {
            index = size-1;
        } else if (size>1 && lineNumAt(size-2)==lineNum) {
            index = size-2;
        } else {
            index = findLineNum(lineNum);
            if (index < 0)
                throw new IllegalStateException(String.format("There is no entry for lineNum==%d",lineNum));
        }
        return changeValueAt(index, newVal);
    }

    private int findLineNum(int lineNum) {
        int lb = 0;
        int ub = size;
        while (lb < ub) {
            int index = (lb + ub) >>> 1;
            int chk = lineNumAt(index);
            if (chk == lineNum)
                return index;
            if (chk > lineNum)
                ub = index;
            else
                lb = index + 1;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private T valueAt(int indx) {
        int pageIndx = indx >> PAGE_BITS;
        int pageOffset = indx & (PAGE_SIZE - 1);
        return (T) vals.get(pageIndx)[pageOffset];
    }
    @SuppressWarnings("unchecked")
    private T changeValueAt(int indx, T newVal) {
        int pageIndx = indx >> PAGE_BITS;
        int pageOffset = indx & (PAGE_SIZE - 1);
        Object[] page = vals.get(pageIndx);
        T oldVal = (T)page[pageOffset];
        page[pageOffset] = newVal;
        return oldVal;
    }

    private int lineNumAt(int indx) {
        int pageIndx = indx >> PAGE_BITS;
        int pageOffset = indx & (PAGE_SIZE - 1);
        return lineNums.get(pageIndx)[pageOffset];
    }
}
