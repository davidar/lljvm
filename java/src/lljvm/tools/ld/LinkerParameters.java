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
import java.util.Collections;
import java.util.List;

public class LinkerParameters {
    
    private final List<AsmSource> sources = new ArrayList<AsmSource>();
    
    private final List<String> libraryClasses = new ArrayList<String>();
    
    private String unresolvedTarget;
    
    
    public LinkerParameters() {
    }
    
    
    public String getUnresolvedTarget() {
        return unresolvedTarget;
    }
    public void setUnresolvedTarget(String unresolvedTarget) {
        this.unresolvedTarget = unresolvedTarget;
    }
    public List<AsmSource> getSources() {
        return Collections.unmodifiableList(new ArrayList<AsmSource>(sources));
    }
    public void addSource(AsmSource src) {
        sources.add(src);
    }
    public List<String> getLibraryClasses() {
        return Collections.unmodifiableList(new ArrayList<String>(libraryClasses));
    }
    public void addLibraryClass(String className) {
        libraryClasses.add(className);
    }
    
    
    

    
    
    

}
