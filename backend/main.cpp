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
#include <llvm/IR/LLVMContext.h>
#include <llvm/PassManager.h>
#include <llvm/Support/CommandLine.h>
#include <llvm/Support/FormattedStream.h>
#include <llvm/Support/system_error.h>
#include <llvm/Support/MemoryBuffer.h>
#include <llvm/IR/DataLayout.h>
#include <llvm/Transforms/Scalar.h>

using namespace llvm;

static RegisterPass<JVMWriter> tmp("JVMWriter", "Generate java bytecode");

static cl::opt<std::string> input(
    cl::Positional, cl::desc("<input bitcode>"), cl::init("-"));
static cl::opt<std::string> classname(
    "classname", cl::desc("Binary name of the generated class"));

enum DebugLevel {g0 = 0, g1 = 1, g2 = 2, g3 = 3};
cl::opt<DebugLevel> debugLevel(cl::desc("Debugging level:"), cl::init(g1),
    cl::values(
    clEnumValN(g2, "g", "Same as -g2"),
    clEnumVal(g0, "No debugging information"),
    clEnumVal(g1, "Source file and line number information (default)"),
    clEnumVal(g2, "-g1 + Local variable information"),
    clEnumVal(g3, "-g2 + Commented LLVM assembly"),
    clEnumValEnd));

int main(int argc, char **argv) {
    cl::ParseCommandLineOptions(argc, argv, "LLJVM Backend\n");
    std::string err;
	OwningPtr<MemoryBuffer> buf;
	error_code errcode;

    errcode = MemoryBuffer::getFileOrSTDIN(input, buf);
    if(errcode) {
        std::cerr << "Unable to open bitcode file: " << errcode << std::endl;
        return 1;
    }
    
    Module *mod = ParseBitcodeFile(buf.get(), getGlobalContext(), &err);
    if(!mod) {
        std::cerr << "Unable to parse bitcode file: " << err << std::endl;
        return 1;
    }

    // Initialize passes
    PassRegistry &Registry = *PassRegistry::getPassRegistry();
    initializeCore(Registry);
    initializeScalarOpts(Registry);
    initializeObjCARCOpts(Registry);
    initializeAnalysis(Registry);
    initializeIPA(Registry);
    initializeTransformUtils(Registry);
    initializeInstCombine(Registry);
    initializeTarget(Registry);

    PassManager pm;
    DataLayout td("e-p:32:32:32"
                  "-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64"
                  "-f32:32:32-f64:64:64");
	JVMWriter *jvw;
    pm.add(new DataLayout(td));
    pm.add(createVerifierPass());
    pm.add(createGCLoweringPass());
    // TODO: fix switch generation so the following pass is not needed
    pm.add(createLowerSwitchPass());
    pm.add(createCFGSimplificationPass());
	jvw = (JVMWriter *)tmp.createPass();
	jvw->Setup(&td, classname, debugLevel);
    pm.add(jvw);
//    pm.add(createGCInfoDeleter());
    pm.run(*mod);
    return 0;
}
