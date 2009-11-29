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

#include <iostream>

#include <llvm/Analysis/Verifier.h>
#include <llvm/Bitcode/ReaderWriter.h>
#include <llvm/CodeGen/Passes.h>
#include <llvm/Support/FormattedStream.h>
#include <llvm/Support/MemoryBuffer.h>
#include <llvm/Target/TargetData.h>
#include <llvm/Transforms/Scalar.h>
#include <llvm/PassManager.h>

using namespace llvm;

int main(int argc, char **argv) {
    if(argc < 2) {
        std::cerr << "Must specify a bitcode file" << std::endl;
        return 1;
    }
    
    std::string err;
    MemoryBuffer *buf = MemoryBuffer::getFileOrSTDIN(argv[1], &err);
    if(!buf) {
        std::cerr << "Unable to open bitcode file: " << err << std::endl;
        return 1;
    }
    
    Module *mod = ParseBitcodeFile(buf, getGlobalContext(), &err);
    if(!mod) {
        std::cerr << "Unable to parse bitcode file: " << err << std::endl;
        return 1;
    }
    
    PassManager pm;
    TargetData td("e-p:32:32:32"
                  "-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:32:32"
                  "-f32:32:32-f64:32:32");
    pm.add(new TargetData(td));
    pm.add(createVerifierPass());
    pm.add(createGCLoweringPass());
    pm.add(createCFGSimplificationPass());
    pm.add(new JVMWriter(&td, fouts()));
    pm.add(createGCInfoDeleter());
    pm.run(*mod);
    return 0;
}
