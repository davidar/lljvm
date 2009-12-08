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

char JVMWriter::id = 0;

JVMWriter::JVMWriter(const TargetData *td, formatted_raw_ostream &o,
                     const std::string &cls)
    : FunctionPass(&id), targetData(td), out(o), classname(cls) {}

void JVMWriter::getAnalysisUsage(AnalysisUsage &au) const {
    au.addRequired<LoopInfo>();
    au.setPreservesAll();
}

bool JVMWriter::runOnFunction(Function &f) {
    if(!f.isDeclaration() && !f.hasAvailableExternallyLinkage())
        printFunction(f);
    return false;
}

bool JVMWriter::doInitialization(Module &m) {
    module = &m;
    instNum = 0;
    
    std::string modID = module->getModuleIdentifier();
    size_t slashPos = modID.rfind('/');
    if(slashPos == std::string::npos)
        sourcename = modID;
    else
        sourcename = modID.substr(slashPos + 1);
    
    if(!classname.empty()) {
        for(std::string::iterator i = classname.begin(),
                                  e = classname.end(); i != e; i++)
            if(*i == '.') *i = '/';
    } else
        classname = sourcename.substr(0, sourcename.find('.'));
    
    printHeader();
    printFields();
    printExternalMethods();
    printConstructor();
    printClInit();
    printMainMethod();
    return false;
}

bool JVMWriter::doFinalization(Module &m) {
    return false;
}
