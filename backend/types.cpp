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

unsigned int JVMWriter::getBitWidth(const Type *ty, bool expand) {
    if(ty->getTypeID() == Type::ArrayTyID
    || ty->getTypeID() == Type::VectorTyID
    || ty->getTypeID() == Type::StructTyID
    || ty->getTypeID() == Type::PointerTyID)
        return 32;

    unsigned int n = ty->getPrimitiveSizeInBits();
    switch(n) {
    case 1:
    case 8:
    case 16:
    case 32: if(expand) return 32;
    case 64: return n;
    default:
        errs() << "Bits = " << n << '\n';
        llvm_unreachable("Unsupported integer width");
    }
}

char JVMWriter::getTypeID(const Type *ty, bool expand) {
    switch(ty->getTypeID()) {
    case Type::VoidTyID:
        return 'V';
    case Type::IntegerTyID:
        switch(getBitWidth(ty, expand)) {
        case  1: return 'Z';
        case  8: return 'B';
        case 16: return 'S';
        case 32: return 'I';
        case 64: return 'J';
        }
    case Type::FloatTyID:
        return 'F';
    case Type::DoubleTyID:
        return 'D';
    case Type::PointerTyID:
    case Type::StructTyID:
    case Type::ArrayTyID:
    case Type::VectorTyID:
        return 'I';
    default:
        errs() << "Type = " << *ty << '\n';
        llvm_unreachable("Invalid type");
    }
}

std::string JVMWriter::getTypeName(const Type *ty, bool expand) {
    switch(getTypeID(ty, expand)) {
    case 'V': return "void";
    case 'Z': return "boolean";
    case 'B': return "byte";
    case 'S': return "short";
    case 'I': return "int";
    case 'J': return "long";
    case 'F': return "float";
    case 'D': return "double";
    }
}

std::string JVMWriter::getTypeDescriptor(const Type *ty, bool expand) {
    return std::string() + getTypeID(ty, expand);
}

std::string JVMWriter::getTypePostfix(const Type *ty, bool expand) {
    switch(ty->getTypeID()) {
    case Type::VoidTyID:
        return "void";
    case Type::IntegerTyID:
        return "i" + utostr(getBitWidth(ty, expand));
    case Type::FloatTyID:
        return "f32";
    case Type::DoubleTyID:
        return "f64";
    case Type::PointerTyID:
    case Type::StructTyID:
    case Type::ArrayTyID:
    case Type::VectorTyID:
        return "i32";
    default:
        errs() << "TypeID = " << ty->getTypeID() << '\n';
        llvm_unreachable("Invalid type");
    }
}

std::string JVMWriter::getTypePrefix(const Type *ty, bool expand) {
    switch(getTypeID(ty, expand)) {
    case 'Z':
    case 'B': return "b";
    case 'S': return "s";
    case 'I': return "i";
    case 'J': return "l";
    case 'F': return "f";
    case 'D': return "d";
    case 'V': llvm_unreachable("void has no prefix");
    }
}
