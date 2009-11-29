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

static unsigned int alignOffset(unsigned int offset, unsigned int align) {
    return offset + ((align - (offset % align)) % align);
}

void JVMWriter::printCmpInstruction(unsigned int predicate,
                                    const Value *left,
                                    const Value *right) {
    std::string inst;
    switch(predicate) {
    case ICmpInst::ICMP_EQ:  inst = "icmp_eq";  break;
    case ICmpInst::ICMP_NE:  inst = "icmp_ne";  break;
    case ICmpInst::ICMP_ULE: inst = "icmp_ule"; break;
    case ICmpInst::ICMP_SLE: inst = "icmp_sle"; break;
    case ICmpInst::ICMP_UGE: inst = "icmp_uge"; break;
    case ICmpInst::ICMP_SGE: inst = "icmp_sge"; break;
    case ICmpInst::ICMP_ULT: inst = "icmp_ult"; break;
    case ICmpInst::ICMP_SLT: inst = "icmp_slt"; break;
    case ICmpInst::ICMP_UGT: inst = "icmp_ugt"; break;
    case ICmpInst::ICMP_SGT: inst = "icmp_sgt"; break;
    case FCmpInst::FCMP_UGT: inst = "fcmp_ugt"; break;
    case FCmpInst::FCMP_OGT: inst = "fcmp_ogt"; break;
    case FCmpInst::FCMP_UGE: inst = "fcmp_uge"; break;
    case FCmpInst::FCMP_OGE: inst = "fcmp_oge"; break;
    case FCmpInst::FCMP_ULT: inst = "fcmp_ult"; break;
    case FCmpInst::FCMP_OLT: inst = "fcmp_olt"; break;
    case FCmpInst::FCMP_ULE: inst = "fcmp_ule"; break;
    case FCmpInst::FCMP_OLE: inst = "fcmp_ole"; break;
    case FCmpInst::FCMP_UEQ: inst = "fcmp_ueq"; break;
    case FCmpInst::FCMP_OEQ: inst = "fcmp_oeq"; break;
    case FCmpInst::FCMP_UNE: inst = "fcmp_une"; break;
    case FCmpInst::FCMP_ONE: inst = "fcmp_one"; break;
    case FCmpInst::FCMP_ORD: inst = "fcmp_ord"; break;
    case FCmpInst::FCMP_UNO: inst = "fcmp_uno"; break;
    default:
        errs() << "Predicate = " << predicate << '\n';
        llvm_unreachable("Invalid cmp predicate");
    }
    printVirtualInstruction(
        inst + "("
        + getTypeDescriptor(left->getType(), true)
        + getTypeDescriptor(right->getType(), true)
        + ")Z", left, right);
}

void JVMWriter::printArithmeticInstruction(unsigned int op,
                                           const Value *left,
                                           const Value *right) {
    std::string typePrefix = getTypePrefix(left->getType(), true);
    std::string typeDescriptor = getTypeDescriptor(left->getType());
    switch(op) {
    case Instruction::Add:
    case Instruction::FAdd:
        printBinaryInstruction(typePrefix + "add",  left, right); break;
    case Instruction::Sub:
    case Instruction::FSub:
        printBinaryInstruction(typePrefix + "sub",  left, right); break;
    case Instruction::Mul:
    case Instruction::FMul:
        printBinaryInstruction(typePrefix + "mul",  left, right); break;
    case Instruction::SDiv:
    case Instruction::FDiv:
        printBinaryInstruction(typePrefix + "div",  left, right); break;
    case Instruction::SRem:
    case Instruction::FRem:
        printBinaryInstruction(typePrefix + "rem",  left, right); break;
    case Instruction::And:
        printBinaryInstruction(typePrefix + "and",  left, right); break;
    case Instruction::Or:
        printBinaryInstruction(typePrefix + "or",   left, right); break;
    case Instruction::Xor:
        printBinaryInstruction(typePrefix + "xor",  left, right); break;
    case Instruction::Shl:
        printBinaryInstruction(typePrefix + "shl",  left, right); break;
    case Instruction::LShr:
        printBinaryInstruction(typePrefix + "ushr", left, right); break;
    case Instruction::AShr:
        printBinaryInstruction(typePrefix + "shr",  left, right); break;
    case Instruction::UDiv:
        printVirtualInstruction("udiv(" + typeDescriptor + typeDescriptor + ")"
            + typeDescriptor, left, right);
        break;
    case Instruction::URem:
        printVirtualInstruction("urem(" + typeDescriptor + typeDescriptor + ")"
            + typeDescriptor, left, right);
        break;
    }
}

void JVMWriter::printCastInstruction(const std::string &typePrefix,
                                     const std::string &srcTypePrefix) {
    if(srcTypePrefix != typePrefix)
        printSimpleInstruction(srcTypePrefix + "2" + typePrefix);
}

void JVMWriter::printCastInstruction(unsigned int op, const Value *v,
                                     const Type *ty, const Type *srcTy) {
    printValueLoad(v);
    switch(op) {
    case Instruction::SIToFP:
    case Instruction::FPToSI:
    case Instruction::FPTrunc:
    case Instruction::FPExt:
    case Instruction::SExt:
        printCastInstruction(getTypePrefix(ty, true),
                             getTypePrefix(srcTy, true)); break;
    case Instruction::Trunc:
        printCastInstruction(getTypePrefix(ty),
                             getTypePrefix(srcTy, true)); break;
    case Instruction::IntToPtr:
        printCastInstruction("i", getTypePrefix(srcTy, true)); break;
    case Instruction::PtrToInt:
        printCastInstruction(getTypePrefix(ty), "i"); break;
    case Instruction::ZExt:
        printVirtualInstruction("zext_" + getTypePostfix(ty, true)
            + "(" + getTypeDescriptor(srcTy) + ")"
            + getTypeDescriptor(ty, true));
        break;
    case Instruction::UIToFP:
        printVirtualInstruction("uitofp_" + getTypePostfix(ty)
            + "(" + getTypeDescriptor(srcTy) + ")" + getTypeDescriptor(ty));
        break;
    case Instruction::FPToUI:
        printVirtualInstruction("fptoui_" + getTypePostfix(ty)
            + "(" + getTypeDescriptor(srcTy) + ")" + getTypeDescriptor(ty));
        break;
    case Instruction::BitCast:
        break;
    default:
        errs() << "Opcode = " << op << '\n';
        llvm_unreachable("Invalid cast instruction");
    }
}

void JVMWriter::printGepInstruction(const Value *v,
                                    gep_type_iterator i,
                                    gep_type_iterator e) {
    // load address
    printCastInstruction(Instruction::IntToPtr, v, NULL, v->getType());
    
    // calculate offset
    for(; i != e; i++){
        unsigned int size = 0;
        const Value *indexValue = i.getOperand();
        
        if(const StructType *structTy = dyn_cast<StructType>(*i)) {
            for(unsigned int f = 0,
                    fieldIndex = cast<ConstantInt>(indexValue)->getZExtValue();
                f < fieldIndex; f++)
                size = alignOffset(
                    size + targetData->getTypeAllocSize(
                        structTy->getContainedType(f)),
                    targetData->getABITypeAlignment(
                        structTy->getContainedType(f + 1)));
            printPtrLoad(size);
            printSimpleInstruction("iadd");
        } else {
            if(const SequentialType *seqTy = dyn_cast<SequentialType>(*i))
                size = targetData->getTypeAllocSize(seqTy->getElementType());
            else
                size = targetData->getTypeAllocSize(*i);
            
            if(const ConstantInt *c = dyn_cast<ConstantInt>(indexValue)) {
                // constant optimisation
                if(c->isNullValue()) {
                    // do nothing
                } else if(c->getValue().isNegative()) {
                    printPtrLoad(c->getValue().abs().getZExtValue() * size);
                    printSimpleInstruction("isub");
                } else {
                    printPtrLoad(c->getZExtValue() * size);
                    printSimpleInstruction("iadd");
                }
            } else {
                printPtrLoad(size);
                printCastInstruction(Instruction::IntToPtr, indexValue,
                                     NULL, indexValue->getType());
                printSimpleInstruction("imul");
                printSimpleInstruction("iadd");
            }
        }
    }
}

void JVMWriter::printVAArgInstruction(const VAArgInst *inst) {
    printIndirectLoad(inst->getOperand(0));
    printSimpleInstruction("dup");
    printConstLoad(
        APInt(32, targetData->getTypeAllocSize(inst->getType()), false));
    printSimpleInstruction("iadd");
    printValueLoad(inst->getOperand(0));
    printSimpleInstruction("swap");
    printIndirectStore(PointerType::getUnqual(
        IntegerType::get(inst->getContext(), 8)));
    printIndirectLoad(inst->getType());
}

void JVMWriter::printAllocaInstruction(const AllocaInst *inst) {
    uint64_t size = targetData->getTypeAllocSize(inst->getAllocatedType());
    if(const ConstantInt *c = dyn_cast<ConstantInt>(inst->getOperand(0))) {
        // constant optimization
        printPtrLoad(c->getZExtValue() * size);
    } else {
        printPtrLoad(size);
        printValueLoad(inst->getOperand(0));
        printSimpleInstruction("imul");
    }
    printSimpleInstruction("invokestatic",
                           "lljvm/runtime/Memory/allocateStack(I)I");
}
