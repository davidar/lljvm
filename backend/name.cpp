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

#include "backend.h"

#include <llvm/Support/Mangler.h>

/**
 * Replace any non-alphanumeric characters with underscores.
 * 
 * @param name  the name to sanitize
 * @return      the sanitized name
 */
std::string JVMWriter::sanitizeName(std::string name) {
    for(std::string::iterator i = name.begin(), e = name.end(); i != e; i++)
        if(!isalnum(*i))
            *i = '_';
    return name;
}

/**
 * Return the name of the given value.
 * 
 * @param v  the value
 * @return   the name of the value
 */
std::string JVMWriter::getValueName(const Value *v) {
    if(const GlobalValue *gv = dyn_cast<GlobalValue>(v))
        return sanitizeName(Mangler(*module).getMangledName(gv));
    if(v->hasName())
        return '_' + sanitizeName(v->getName());
    if(localVars.count(v))
        return '_' + utostr(getLocalVarNumber(v));
    return "_";
}
/**
 * Return the label name of the given block.
 * 
 * @param v  the block
 * @return   the label
 */
std::string JVMWriter::getLabelName(const Value *v) {
    if(const GlobalValue *gv = dyn_cast<GlobalValue>(v))
        return sanitizeName(Mangler(*module).getMangledName(gv));
    
    if(!anonValues.count(v))
        anonValues[v] = anonValues.size() + 1;
    return sanitizeName("label" + utostr(anonValues[v]));
}
