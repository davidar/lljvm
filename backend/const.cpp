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

/**
 * Load the given pointer.
 * 
 * @param n  the value of the pointer
 */
void JVMWriter::printPtrLoad(uint64_t n) {
    if(module->getPointerSize() != Module::Pointer32)
        llvm_unreachable("Only 32-bit pointers are allowed");
    printConstLoad(APInt(32, n, false));
}

/**
 * Load the given integer.
 * 
 * @param i  the integer
 */
void JVMWriter::printConstLoad(const APInt &i) {
    if(i.getBitWidth() <= 32) {
        int64_t value = i.getSExtValue();
        if(value == -1)
            printSimpleInstruction("iconst_m1");
        else if(0 <= value && value <= 5)
            printSimpleInstruction("iconst_" + i.toString(10, true));
        else if(-0x80 <= value && value <= 0x7f)
            printSimpleInstruction("bipush", i.toString(10, true));
        else if(-0x8000 <= value && value <= 0x7fff)
            printSimpleInstruction("sipush", i.toString(10, true));
        else
            printSimpleInstruction("ldc", i.toString(10, true));
    } else {
        if(i == 0)
            printSimpleInstruction("lconst_0");
        else if(i == 1)
            printSimpleInstruction("lconst_1");
        else
            printSimpleInstruction("ldc2_w", i.toString(10, true));
    }
}

/**
 * Load the given single-precision floating point value.
 * 
 * @param f  the value
 */
void JVMWriter::printConstLoad(float f) {
    if(f == 0.0)
        printSimpleInstruction("fconst_0");
    else if(f == 1.0)
        printSimpleInstruction("fconst_1");
    else if(f == 2.0)
        printSimpleInstruction("fconst_2");
    else if(IsNAN(f))
        printSimpleInstruction("getstatic", "java/lang/Float/NaN F");
    else if(IsInf(f) > 0)
        printSimpleInstruction("getstatic",
                               "java/lang/Float/POSITIVE_INFINITY F");
    else if(IsInf(f) < 0)
        printSimpleInstruction("getstatic",
                               "java/lang/Float/NEGATIVE_INFINITY F");
    else
        printSimpleInstruction("ldc", ftostr(f));
}

/**
 * Load the given double-precision floating point value.
 * 
 * @param d  the value
 */
void JVMWriter::printConstLoad(double d) {
    if(d == 0.0)
        printSimpleInstruction("dconst_0");
    else if(d == 1.0)
        printSimpleInstruction("dconst_1");
    else if(IsNAN(d))
        printSimpleInstruction("getstatic", "java/lang/Double/NaN D");
    else if(IsInf(d) > 0)
        printSimpleInstruction("getstatic",
                               "java/lang/Double/POSITIVE_INFINITY D");
    else if(IsInf(d) < 0)
        printSimpleInstruction("getstatic",
                               "java/lang/Double/NEGATIVE_INFINITY D");
    else
        printSimpleInstruction("ldc2_w", ftostr(d));
}

/**
 * Load the given constant.
 * 
 * @param c  the constant
 */
void JVMWriter::printConstLoad(const Constant *c) {
    if(const ConstantInt *i = dyn_cast<ConstantInt>(c)) {
        printConstLoad(i->getValue());
    } else if(const ConstantFP *fp = dyn_cast<ConstantFP>(c)) {
        if(fp->getType()->getTypeID() == Type::FloatTyID)
            printConstLoad(fp->getValueAPF().convertToFloat());
        else
            printConstLoad(fp->getValueAPF().convertToDouble());
    } else if(isa<UndefValue>(c)) {
        printPtrLoad(0);
    } else {
        errs() << "Constant = " << *c << '\n';
        llvm_unreachable("Invalid constant value");
    }
}

/**
 * Load the given string.
 * 
 * @param str      the string
 * @param cstring  true iff the string contains a single null character at the
 *                 end
 */
void JVMWriter::printConstLoad(const std::string &str, bool cstring) {
    out << "\tldc \"";
    if(cstring)
        for(std::string::const_iterator i = str.begin(),
                                        e = str.end()-1; i != e; i++)
            switch(*i) {
            case '\\': out << "\\\\"; break;
            case '\b': out << "\\b";  break;
            case '\t': out << "\\t";  break;
            case '\n': out << "\\n";  break;
            case '\f': out << "\\f";  break;
            case '\r': out << "\\r";  break;
            case '\"': out << "\\\""; break;
            case '\'': out << "\\\'"; break;
            default:   out << *i;     break;
            }
    else
        for(std::string::const_iterator i = str.begin(),
                                        e = str.end(); i != e; i++) {
            const char c = *i;
            out << "\\u00" << hexdigit((c>>4) & 0xf) << hexdigit(c & 0xf);
        }
    out << "\"\n";
}

/**
 * Store the given static constant. The constant is stored to the address
 * currently on top of the stack, pushing the first address following the
 * constant onto the stack afterwards.
 * 
 * @param c  the constant
 */
void JVMWriter::printStaticConstant(const Constant *c) {
    if(isa<ConstantAggregateZero>(c) || c->isNullValue()) {
        // zero initialised constant
        printPtrLoad(targetData->getTypeAllocSize(c->getType()));
        printSimpleInstruction("invokestatic",
                               "lljvm/runtime/Memory/zero(II)I");
        return;
    }
    std::string typeDescriptor = getTypeDescriptor(c->getType());
    switch(c->getType()->getTypeID()) {
    case Type::IntegerTyID:
    case Type::FloatTyID:
    case Type::DoubleTyID:
        printConstLoad(c);
        printSimpleInstruction("invokestatic",
            "lljvm/runtime/Memory/pack(I" + typeDescriptor + ")I");
        break;
    case Type::ArrayTyID:
        if(const ConstantArray *ca = dyn_cast<ConstantArray>(c))
            if(ca->isString()) {
                bool cstring = ca->isCString();
                printConstLoad(ca->getAsString(), cstring);
                if(cstring)
                    printSimpleInstruction("invokestatic",
                        "lljvm/runtime/Memory/pack(ILjava/lang/String;)I");
                else {
                    printSimpleInstruction("invokevirtual", 
                                           "java/lang/String/toCharArray()[C");
                    printSimpleInstruction("invokestatic",
                                           "lljvm/runtime/Memory/pack(I[C)I");
                }
                break;
            }
        // else fall through
    case Type::VectorTyID:
    case Type::StructTyID:
        for(unsigned int i = 0, e = c->getNumOperands(); i < e; i++)
            printStaticConstant(cast<Constant>(c->getOperand(i)));
        break;
    case Type::PointerTyID:
        if(const Function *f = dyn_cast<Function>(c))
            printValueLoad(f);
        else if(const GlobalVariable *g = dyn_cast<GlobalVariable>(c))
            // initialise with address of global variable
            printValueLoad(g);
        else if(isa<ConstantPointerNull>(c) || c->isNullValue())
            printSimpleInstruction("iconst_0");
        else if(const ConstantExpr *ce = dyn_cast<ConstantExpr>(c))
            printConstantExpr(ce);
        else {
            errs() << "Constant = " << *c << '\n';
            llvm_unreachable("Invalid static initializer");
        }
        printSimpleInstruction("invokestatic",
            "lljvm/runtime/Memory/pack(I" + typeDescriptor + ")I");
        break;
    default:
        errs() << "TypeID = " << c->getType()->getTypeID() << '\n';
        llvm_unreachable("Invalid type in printStaticConstant()");
    }
}

/**
 * Print the given constant expression.
 * 
 * @param ce  the constant expression
 */
void JVMWriter::printConstantExpr(const ConstantExpr *ce) {
    const Value *left, *right;
    if(ce->getNumOperands() >= 1) left  = ce->getOperand(0);
    if(ce->getNumOperands() >= 2) right = ce->getOperand(1);
    switch(ce->getOpcode()) {
    case Instruction::Trunc:
    case Instruction::ZExt:
    case Instruction::SExt:
    case Instruction::FPTrunc:
    case Instruction::FPExt:
    case Instruction::UIToFP:
    case Instruction::SIToFP:
    case Instruction::FPToUI:
    case Instruction::FPToSI:
    case Instruction::PtrToInt:
    case Instruction::IntToPtr:
    case Instruction::BitCast:
        printCastInstruction(ce->getOpcode(), left,
                             ce->getType(), left->getType()); break;
    case Instruction::Add:
    case Instruction::FAdd:
    case Instruction::Sub:
    case Instruction::FSub:
    case Instruction::Mul:
    case Instruction::FMul:
    case Instruction::UDiv:
    case Instruction::SDiv:
    case Instruction::FDiv:
    case Instruction::URem:
    case Instruction::SRem:
    case Instruction::FRem:
    case Instruction::And:
    case Instruction::Or:
    case Instruction::Xor:
    case Instruction::Shl:
    case Instruction::LShr:
    case Instruction::AShr:
        printArithmeticInstruction(ce->getOpcode(), left, right); break;
    case Instruction::ICmp:
    case Instruction::FCmp:
        printCmpInstruction(ce->getPredicate(), left, right); break;
    case Instruction::GetElementPtr:
        printGepInstruction(ce->getOperand(0),
                            gep_type_begin(ce),
                            gep_type_end(ce)); break;
    case Instruction::Select:
        printSelectInstruction(ce->getOperand(0),
                               ce->getOperand(1),
                               ce->getOperand(2)); break;
    default:
        errs() << "Expression = " << *ce << '\n';
        llvm_unreachable("Invalid constant expression");
    }
}
