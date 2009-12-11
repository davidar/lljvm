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

std::string JVMWriter::getCallSignature(const FunctionType *ty) {
    std::string sig;
    sig += '(';
    for(unsigned int i = 0, e = ty->getNumParams(); i < e; i++)
        sig += getTypeDescriptor(ty->getParamType(i));
    if(ty->isVarArg()) sig += "I";
    sig += ')';
    sig += getTypeDescriptor(ty->getReturnType());
    return sig;
}

void JVMWriter::printOperandPack(const Instruction *inst,
                                 unsigned int minOperand,
                                 unsigned int maxOperand) {
    unsigned int size = 0;
    for(unsigned int i = minOperand; i < maxOperand; i++)
        size += targetData->getTypeAllocSize(
            inst->getOperand(i)->getType());

    printSimpleInstruction("bipush", utostr(size));
    printSimpleInstruction("invokestatic",
                           "lljvm/runtime/Memory/allocateStack(I)I");
    printSimpleInstruction("dup");

    for(unsigned int i = minOperand; i < maxOperand; i++) {
        const Value *v = inst->getOperand(i);
        printValueLoad(v);
        printSimpleInstruction("invokestatic",
            "lljvm/runtime/Memory/pack(I"
            + getTypeDescriptor(v->getType()) + ")I");
    }
    printSimpleInstruction("pop");
}

void JVMWriter::printFunctionCall(const Value *functionVal,
                                  const Instruction *inst) {
    unsigned int origin = isa<InvokeInst>(inst) ? 3 : 1;
    if(const Function *f = dyn_cast<Function>(functionVal)) { // direct call
        const FunctionType *ty = f->getFunctionType();
        
        //for(unsigned int i = origin, e = inst->getNumOperands(); i < e; i++)
        //    printValueLoad(inst->getOperand(i));
        
        for(unsigned int i = 0, e = ty->getNumParams(); i < e; i++)
            printValueLoad(inst->getOperand(i + origin));
        if(ty->isVarArg() && inst)
            printOperandPack(inst, ty->getNumParams() + origin,
                                   inst->getNumOperands());
        
        if(externRefs.count(f))
            printSimpleInstruction("invokestatic",
                getValueName(f) + getCallSignature(ty));
        else
            printSimpleInstruction("invokestatic",
                classname + "/" + getValueName(f) + getCallSignature(ty));
    } else { // indirect call
        printValueLoad(functionVal);
        const FunctionType *ty = cast<FunctionType>(
            cast<PointerType>(functionVal->getType())->getElementType());
        printOperandPack(inst, origin, inst->getNumOperands());
        printSimpleInstruction("invokestatic",
            "lljvm/runtime/Function/invoke_"
            + getTypePostfix(ty->getReturnType()) + "(II)"
            + getTypeDescriptor(ty->getReturnType()));
    }
}

void JVMWriter::printIntrinsicCall(const IntrinsicInst *inst) {
    switch(inst->getIntrinsicID()) {
    case Intrinsic::vastart:
    case Intrinsic::vacopy:
    case Intrinsic::vaend:
        printVAIntrinsic(inst); break;
    case Intrinsic::memcpy:
    case Intrinsic::memmove:
    case Intrinsic::memset:
        printMemIntrinsic(cast<MemIntrinsic>(inst)); break;
    case Intrinsic::flt_rounds:
        printSimpleInstruction("iconst_m1"); break;
    case Intrinsic::dbg_declare:
    case Intrinsic::dbg_func_start:
    case Intrinsic::dbg_region_end:
    case Intrinsic::dbg_region_start:
    case Intrinsic::dbg_stoppoint:
        // ignore debugging intrinsics
        break;
    default:
    errs() << "Intrinsic = " << *inst << '\n';
    llvm_unreachable("Invalid intrinsic function");
  }
}

void JVMWriter::printCallInstruction(const Instruction *inst) {
    if(isa<IntrinsicInst>(inst))
        printIntrinsicCall(cast<IntrinsicInst>(inst));
    else
        printFunctionCall(inst->getOperand(0), inst);
}

void JVMWriter::printInvokeInstruction(const InvokeInst *inst) {
    std::string labelname = getLabelName(inst) + "$invoke";
    printLabel(labelname + "_begin");
    printFunctionCall(inst->getOperand(0), inst);
    printValueStore(inst); // save return value
    printLabel(labelname + "_end");
    printBranchToBlock(inst->getParent(), NULL, inst->getNormalDest());
    printLabel(labelname + "_catch");
    printSimpleInstruction("pop");
    printBranchToBlock(inst->getParent(), NULL, inst->getUnwindDest());
    printSimpleInstruction(".catch lljvm/runtime/System$Unwind",
          "from "  + labelname + "_begin "
        + "to "    + labelname + "_end "
        + "using " + labelname + "_catch");
}

void JVMWriter::printLocalVariable(const Function &f,
                                   const Instruction *inst) {
    const Type *ty;
    if(isa<AllocaInst>(inst) && !isa<GlobalVariable>(inst))
        // local variable allocation
        ty = PointerType::getUnqual(
                 cast<AllocaInst>(inst)->getAllocatedType());
    else // operation result
        ty = inst->getType();
    // getLocalVarNumber must be called at least once in this method
    unsigned int varNum = getLocalVarNumber(inst);
#ifdef DEBUG
    printSimpleInstruction(".var " + utostr(varNum) + " is "
        + getValueName(inst) + ' ' + getTypeDescriptor(ty)
        + " from begin_method to end_method");
#endif
    // initialise variable to avoid class verification errors
    printSimpleInstruction(getTypePrefix(ty, true) + "const_0");
    printSimpleInstruction(getTypePrefix(ty, true) + "store", utostr(varNum));
}

void JVMWriter::printFunctionBody(const Function &f) {
    for(Function::const_iterator i = f.begin(), e = f.end(); i != e; i++) {
        if(Loop *l = getAnalysis<LoopInfo>().getLoopFor(i)) {
            if(l->getHeader() == i && l->getParentLoop() == 0)
                printLoop(l);
        } else
            printBasicBlock(i);
    }
}

unsigned int JVMWriter::getLocalVarNumber(const Value *v) {
    if(!localVars.count(v))
        localVars[v] = usedRegisters++;
    if(getBitWidth(v->getType()) == 64)
        usedRegisters++; // 64 bit types occupy 2 registers
    return localVars[v];
}

void JVMWriter::printFunction(const Function &f) {
    localVars.clear();
    usedRegisters = 0;
    
    out << '\n';
    out << ".method " << (f.hasLocalLinkage() ? "private " : "public ")
        << "static " << getValueName(&f) << '(';
    for(Function::const_arg_iterator i = f.arg_begin(), e = f.arg_end();
        i != e; i++)
        out << getTypeDescriptor(i->getType());
    if(f.isVarArg())
        out << "I";
    out << ')' << getTypeDescriptor(f.getReturnType()) << '\n';
    
    for(Function::const_arg_iterator i = f.arg_begin(), e = f.arg_end();
        i != e; i++) {
        // getLocalVarNumber must be called at least once in each iteration
        unsigned int varNum = getLocalVarNumber(i);
#ifdef DEBUG
        printSimpleInstruction(".var " + utostr(varNum) + " is "
            + getValueName(i) + ' ' + getTypeDescriptor(i->getType())
            + " from begin_method to end_method");
#endif
    }
    if(f.isVarArg()) {
        vaArgNum = usedRegisters++;
#ifdef DEBUG
        printSimpleInstruction(".var " + utostr(vaArgNum)
            + " is varargptr I from begin_method to end_method");
#endif
    }
    
    // TODO: better stack depth analysis
    unsigned int stackDepth = 8;
    for(const_inst_iterator i = inst_begin(&f), e = inst_end(&f);
        i != e; i++) {
        if(stackDepth < i->getNumOperands())
            stackDepth = i->getNumOperands();
        if(i->getType() != Type::getVoidTy(f.getContext()))
            printLocalVariable(f, &*i);
    }
    printSimpleInstruction(".limit stack", utostr(stackDepth * 2));
    printSimpleInstruction(".limit locals", utostr(usedRegisters));
    
    printLabel("begin_method");
    printSimpleInstruction("invokestatic",
                           "lljvm/runtime/Memory/createStackFrame()V");
    printFunctionBody(f);
    printLabel("end_method");
    out << ".end method\n";
}
