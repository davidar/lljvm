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

/**
 * Constructor.
 * 
 * @param td   the target data for the platform
 * @param o    the output stream to be written to
 * @param cls  the binary name of the class to generate
 * @param dbg  the debugging level
 */
JVMWriter::JVMWriter(const TargetData *td, formatted_raw_ostream &o,
                     const std::string &cls, unsigned int dbg)
    : FunctionPass(&id), targetData(td), out(o), classname(cls), debug(dbg) {}

/**
 * Register required analysis information.
 * 
 * @param au  AnalysisUsage object representing the analysis usage information
 *            of this pass.
 */
void JVMWriter::getAnalysisUsage(AnalysisUsage &au) const {
    au.addRequired<LoopInfo>();
    au.setPreservesAll();
}

/**
 * Process the given function.
 * 
 * @param f  the function to process
 * @return   whether the function was modified (always false)
 */
bool JVMWriter::runOnFunction(Function &f) {
    if(!f.isDeclaration() && !f.hasAvailableExternallyLinkage())
        printFunction(f);
    return false;
}

/**
 * Perform per-module initialization.
 * 
 * @param m  the module
 * @return   whether the module was modified (always false)
 */
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
    } else {
        classname = sourcename.substr(0, sourcename.rfind('.'));
        for(std::string::iterator i = classname.begin(),
                                  e = classname.end(); i != e; i++)
            if(*i == '.') *i = '_';
    }
    
    printHeader();
    printFields();
    printExternalMethods();
    printConstructor();
    printClInit();
    printMainMethod();
    return false;
}

/**
 * Perform per-module finalization.
 * 
 * @param m  the module
 * @return   whether the module was modified (always false)
 */
bool JVMWriter::doFinalization(Module &m) {
    return false;
}
