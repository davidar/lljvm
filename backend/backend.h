/*
* Copyright (c) 2009-2010 David Roberts <d@vidr.cc>
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

#ifndef BACKEND_H
#define BACKEND_H

#include <llvm/ADT/DenseMap.h>
#include <llvm/ADT/DenseSet.h>
#include <llvm/ADT/StringExtras.h>
#include <llvm/Analysis/ConstantsScanner.h>
#include <llvm/Analysis/LoopInfo.h>
#include <llvm/Instructions.h>
#include <llvm/IntrinsicInst.h>
#include <llvm/Module.h>
#include <llvm/Support/ErrorHandling.h>
#include <llvm/Support/FormattedStream.h>
#include <llvm/Support/GetElementPtrTypeIterator.h>
#include <llvm/Target/TargetData.h>

using namespace llvm;

/**
 * A FunctionPass for generating Jasmin-style assembly for the JVM.
 * 
 * @author  David Roberts
 */
class JVMWriter : public FunctionPass {
    /** The output stream */
    formatted_raw_ostream &out;
    /** The name of the source file */
    std::string sourcename;
    /** The binary name of the generated class */
    std::string classname;
    /** The debugging level */
    unsigned int debug;
    /** The current module */
    Module *module;
    /** The target data for the platform */
    const TargetData *targetData;
    /** Pass ID */
    static char id;
    
    /** Set of external references */
    DenseSet<const Value*> externRefs;
    /** Mapping of blocks to unique IDs */
    DenseMap<const BasicBlock*, unsigned int> blockIDs;
    /** Mapping of values to local variable numbers */
    DenseMap<const Value*, unsigned int> localVars;
    /** Number of registers allocated for the function */
    unsigned int usedRegisters;
    /** Local variable number of the pointer to the packed list of varargs */
    unsigned int vaArgNum;
    /** Current instruction number */
    unsigned int instNum;

public:
    JVMWriter(const TargetData *td, formatted_raw_ostream &o,
              const std::string &cls, unsigned int dbg);

private:
    // backend.cpp
    void getAnalysisUsage(AnalysisUsage &au) const;
    bool runOnFunction(Function &f);
    bool doInitialization(Module &m);
    bool doFinalization(Module &m);
    
    // block.cpp
    void printBasicBlock(const BasicBlock *block);
    void printInstruction(const Instruction *inst);
    
    // branch.cpp
    void printPHICopy(const BasicBlock *src, const BasicBlock *dest);
    void printBranchInstruction(const BasicBlock *curBlock,
                                const BasicBlock *destBlock);
    void printBranchInstruction(const BasicBlock *curBlock,
                                const BasicBlock *trueBlock,
                                const BasicBlock *falseBlock);
    void printBranchInstruction(const BranchInst *inst);
    void printSelectInstruction(const Value *cond,
                                const Value *trueVal,
                                const Value *falseVal);
    void printSwitchInstruction(const SwitchInst *inst);
    void printLoop(const Loop *l);
    
    // const.cpp
    void printPtrLoad(uint64_t n);
    void printConstLoad(const APInt &i);
    void printConstLoad(float f);
    void printConstLoad(double d);
    void printConstLoad(const Constant *c);
    void printConstLoad(const std::string &str, bool cstring);
    void printStaticConstant(const Constant *c);
    void printConstantExpr(const ConstantExpr *ce);
    
    // function.cpp
    std::string getCallSignature(const FunctionType *ty);
    void printOperandPack(const Instruction *inst,
                          unsigned int minOperand,
                          unsigned int maxOperand);
    void printFunctionCall(const Value *functionVal, const Instruction *inst);
    void printIntrinsicCall(const IntrinsicInst *inst);
    void printCallInstruction(const Instruction *inst);
    void printInvokeInstruction(const InvokeInst *inst);
    void printLocalVariable(const Function &f, const Instruction *inst);
    void printFunctionBody(const Function &f);
    unsigned int getLocalVarNumber(const Value *v);
    void printCatchJump(unsigned int numJumps);
    void printFunction(const Function &f);
    
    // instruction.cpp
    void printCmpInstruction(unsigned int predicate,
                             const Value *left,
                             const Value *right);
    void printArithmeticInstruction(unsigned int op,
                                    const Value *left,
                                    const Value *right);
    void printBitCastInstruction(const Type *ty, const Type *srcTy);
    void printCastInstruction(const std::string &typePrefix,
                              const std::string &srcTypePrefix);
    void printCastInstruction(unsigned int op, const Value *v,
                              const Type *ty, const Type *srcTy);
    void printGepInstruction(const Value *v,
                             gep_type_iterator i,
                             gep_type_iterator e);
    void printAllocaInstruction(const AllocaInst *inst);
    void printVAArgInstruction(const VAArgInst *inst);
    void printVAIntrinsic(const IntrinsicInst *inst);
    void printMemIntrinsic(const MemIntrinsic *inst);
    void printMathIntrinsic(const IntrinsicInst *inst);
    void printBitIntrinsic(const IntrinsicInst *inst);
    
    // loadstore.cpp
    void printValueLoad(const Value *v);
    void printValueStore(const Value *v);
    void printIndirectLoad(const Value *v);
    void printIndirectLoad(const Type *ty);
    void printIndirectStore(const Value *ptr, const Value *val);
    void printIndirectStore(const Type *ty);
    
    // name.cpp
    std::string sanitizeName(std::string name);
    std::string getValueName(const Value *v);
    std::string getLabelName(const BasicBlock *block);
    
    // printinst.cpp
    void printBinaryInstruction(const char *name,
                                const Value *left,
                                const Value *right);
    void printBinaryInstruction(const std::string &name,
                                const Value *left,
                                const Value *right);
    void printSimpleInstruction(const char *inst);
    void printSimpleInstruction(const char *inst, const char *operand);
    void printSimpleInstruction(const std::string &inst);
    void printSimpleInstruction(const std::string &inst,
                                const std::string &operand);
    void printVirtualInstruction(const char *sig);
    void printVirtualInstruction(const char *sig, const Value *operand);
    void printVirtualInstruction(const char *sig,
                                 const Value *left,
                                 const Value *right);
    void printVirtualInstruction(const std::string &sig);
    void printVirtualInstruction(const std::string &sig, const Value *operand);
    void printVirtualInstruction(const std::string &sig,
                                 const Value *left,
                                 const Value *right);
    void printLabel(const char *label);
    void printLabel(const std::string &label);
    
    // sections.cpp
    void printHeader();
    void printFields();
    void printExternalMethods();
    void printConstructor();
    void printClInit();
    void printMainMethod();
    
    // types.cpp
    unsigned int getBitWidth(     const Type *ty, bool expand = false);
    char getTypeID(               const Type *ty, bool expand = false);
    std::string getTypeName(      const Type *ty, bool expand = false);
    std::string getTypeDescriptor(const Type *ty, bool expand = false);
    std::string getTypePostfix(   const Type *ty, bool expand = false);
    std::string getTypePrefix(    const Type *ty, bool expand = false);
};

#endif
