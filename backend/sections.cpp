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

#include "backend.h"

/**
 * Print the header.
 */
void JVMWriter::printHeader() {
    if(debug >= 1)
        out << ".source " << sourcename << "\n";
    out << ".class public final " << classname << "\n"
           ".super java/lang/Object\n\n";
}

/**
 * Print the field declarations.
 */
void JVMWriter::printFields() {
    out << "; Fields\n";
    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(i->isDeclaration()) {
            out << ".extern field ";
            externRefs.insert(i);
        } else
            out << ".field "
                << (i->hasLocalLinkage() ? "private " : "public ")
                << "static final ";
        out << getValueName(i) << ' ' << getTypeDescriptor(i->getType());
        if(debug >= 3)
            out << " ; " << *i;
        else
            out << '\n';
    }
    out << '\n';
}

/**
 * Print the list of external methods.
 */
void JVMWriter::printExternalMethods() {
    out << "; External methods\n";
    for(Module::const_iterator i = module->begin(),
                               e = module->end(); i != e; i++) {
        if(i->isDeclaration() && !i->isIntrinsic()) {
            const Function *f = i;
            const FunctionType *ty = f->getFunctionType();
            out << ".extern method "
                << getValueName(f) << getCallSignature(ty);
            if(debug >= 3)
                out << " ; " << *ty;
            out << '\n';
            externRefs.insert(f);
        }
    }
    out << '\n';
}

/**
 * Print the class constructor.
 */
void JVMWriter::printConstructor() {
    out << "; Constructor\n"
           ".method private <init>()V\n"
           "\taload_0\n"
           "\tinvokespecial java/lang/Object/<init>()V\n"
           "\treturn\n"
           ".end method\n\n";
}

/**
 * Print the static class initialization method.
 */
void JVMWriter::printClInit() {
    out << ".method public <clinit>()V\n";
    printSimpleInstruction(".limit stack 4");
    
    out << "\n\t; allocate global variables\n";
    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(!i->isDeclaration()) {
            const GlobalVariable *g = i;
            const Constant *c = g->getInitializer();
            printConstLoad(
                APInt(32, targetData->getTypeAllocSize(c->getType()), false));
            printSimpleInstruction("invokestatic",
                                   "lljvm/runtime/Memory/allocateData(I)I");
            printSimpleInstruction("putstatic",
                classname + "/" + getValueName(g) + " I");
        }
    }
    
    out << "\n\t; initialise global variables\n";
    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(!i->isDeclaration()) {
            const GlobalVariable *g = i;
            const Constant *c = g->getInitializer();
            printSimpleInstruction("getstatic",
                classname + "/" + getValueName(g) + " I");
            printStaticConstant(c);
            printSimpleInstruction("pop");
            out << '\n';
        }
    }
    
    printSimpleInstruction("return");
    out << ".end method\n\n";
}

/**
 * Print the main method.
 */
void JVMWriter::printMainMethod() {
    const Function *f = module->getFunction("main");
    if(!f || f->isDeclaration())
        return;

    out << ".method public static main([Ljava/lang/String;)V\n";
    printSimpleInstruction(".limit stack 4");

    if(f->arg_size() == 0) {
        printSimpleInstruction("invokestatic", classname + "/main()I");
    } else if(f->arg_size() == 2) {
        Function::const_arg_iterator arg1, arg2;
        arg1 = arg2 = f->arg_begin(); arg2++;
        if(!arg1->getType()->isIntegerTy()
         || arg2->getType()->getTypeID() != Type::PointerTyID)
            llvm_unreachable("main function has invalid type signature");
        printSimpleInstruction("aload_0");
        printSimpleInstruction("arraylength");
        printSimpleInstruction("aload_0");
        printSimpleInstruction("invokestatic",
            "lljvm/runtime/Memory/storeStack([Ljava/lang/String;)I");
        printSimpleInstruction("invokestatic", classname + "/main("
            + getTypeDescriptor(arg1->getType())
            + getTypeDescriptor(arg2->getType()) + ")I");
    } else {
        llvm_unreachable("main function has invalid number of arguments");
    }

    printSimpleInstruction("invokestatic", "lljvm/lib/c/exit(I)V");
    printSimpleInstruction("return");
    out << ".end method\n";
}
