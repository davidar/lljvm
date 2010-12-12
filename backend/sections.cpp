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
           ".super java/lang/Object\n"
           ".implements lljvm/runtime/CustomLibrary\n\n";
}

/**
 * Print the field declarations.
 */
void JVMWriter::printFields() {
    out << "; Fields\n";
    out << ".field private final __env Llljvm/runtime/Environment;\n";
    
    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(i->isDeclaration()) {
            out << ".extern field ";
            externRefs.insert(i);
        } else
            out << ".field "
                << (i->hasLocalLinkage() ? "private " : "public ")
                << "final ";
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
           ".method public <init>()V\n"
           "\taload_0\n"
           "\tinvokespecial java/lang/Object/<init>()V\n"
           "\treturn\n"
           ".end method\n\n";
}

void JVMWriter::printLoadThis() {
    printSimpleInstruction("aload_0"); // "this."
}

void JVMWriter::printLoadEnvToStack() {
    printLoadThis();
    printSimpleInstruction("getfield "+classname+"/__env Llljvm/runtime/Environment;");
}

void JVMWriter::printLoadMemoryToStack() {
    printLoadEnvToStack( ); // "__env."
    printSimpleInstruction("getfield lljvm/runtime/Environment/memory Llljvm/runtime/Memory;");
}

void JVMWriter::printLoadFunctionToStack() {
    printLoadEnvToStack();
    printSimpleInstruction("getfield", "lljvm/runtime/Environment/function Llljvm/runtime/Function;");
}

/**
 * Print the static class initialization method.
 */
void JVMWriter::printClInit() {
    //out << ".method public <clinit>()V\n";
    out << ".method public initialiseEnvironment(Llljvm/runtime/Environment;)V\n";
    printSimpleInstruction(".limit stack 5");
    printSimpleInstruction(".limit locals 2");
    
    out << "\n\t; load environment into class\n";
    printSimpleInstruction("aload_0"); // this.
    printSimpleInstruction("aload_1"); // value
    printSimpleInstruction("putfield "+classname+"/__env Llljvm/runtime/Environment;");
    
    out << "\n\t; allocate global variables\n";
    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(!i->isDeclaration()) {
            const GlobalVariable *g = i;
            const Constant *c = g->getInitializer();
            printLoadMemoryToStack();
            printConstLoad(
                APInt(32, targetData->getTypeAllocSize(c->getType()), false));
            printSimpleInstruction("invokevirtual",
                                   "lljvm/runtime/Memory/allocateData(I)I");
            printSimpleInstruction("aload_0"); // "this"
            printSimpleInstruction("swap"); // move this 1 down the stack
            printSimpleInstruction("putfield",
                classname + "/" + getValueName(g) + " I");
        }
    }
    
    out << "\n\t; initialise global variables\n";
    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(!i->isDeclaration()) {
            const GlobalVariable *g = i;
            const Constant *c = g->getInitializer();
            printSimpleInstruction("aload_0"); // "this"
            printSimpleInstruction("getfield",
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
    printSimpleInstruction(".limit stack 5");
    
    out << "\n\t; create an instance of our class, leaving it as TOS\n";
    printSimpleInstruction("new", classname);
    printSimpleInstruction("dup");
    printSimpleInstruction("invokespecial", classname+"/<init>()V");
    // stack: inst
    
    printSimpleInstruction("dup");
    // stack: inst inst
    
    // create our environment
    out << "\n\t; create our environment\n";
    
    printSimpleInstruction("new", "lljvm/runtime/Environment");
    printSimpleInstruction("dup");
    printSimpleInstruction("invokespecial", "lljvm/runtime/Environment/<init>()V");
    // stack: inst inst env
    
    printSimpleInstruction("swap");
    // stack: inst env inst
    
    printSimpleInstruction("invokevirtual", "lljvm/runtime/Environment/loadCustomLibrary("
                                            "Llljvm/runtime/CustomLibrary;"
                                            ")V");
    // stack: inst
    
    printSimpleInstruction("dup");
    
    // stack: inst inst

    if(f->arg_size() == 0) {
        printSimpleInstruction("invokevirtual", classname + "/main()I");
        // stack: inst
    } else if(f->arg_size() == 2) {
        Function::const_arg_iterator arg1, arg2;
        arg1 = arg2 = f->arg_begin(); arg2++;
        if(!arg1->getType()->isIntegerTy()
         || arg2->getType()->getTypeID() != Type::PointerTyID)
            llvm_unreachable("main function has invalid type signature");
        printSimpleInstruction("aload_0");
        printSimpleInstruction("arraylength");
        
        // stack: inst inst argc
        
        printSimpleInstruction("swap");
        printSimpleInstruction("dup_x1");
        
        // stack: inst inst argc inst
        
        // load memory onto stack
        printSimpleInstruction("getfield", classname+"/__env Llljvm/runtime/Environment;");
        // stack: inst inst argc env
        printSimpleInstruction("getfield", "lljvm/runtime/Environment/memory Llljvm/runtime/Memory;");
        // stack: inst inst argc memory
        
        printSimpleInstruction("aload_0");
        printSimpleInstruction("invokevirtual",
            "lljvm/runtime/Memory/storeStack([Ljava/lang/String;)I");
        // stack: inst inst argc argv
        
        printSimpleInstruction("invokevirtual", classname + "/main("
            + getTypeDescriptor(arg1->getType())
            + getTypeDescriptor(arg2->getType()) + ")I");
    } else {
        llvm_unreachable("main function has invalid number of arguments");
    }
    
    // stack: inst ret
    printSimpleInstruction("swap");
    // stack: ret inst
    printSimpleInstruction("getfield", classname+"/__env Llljvm/runtime/Environment;");
    // stack: ret env
    printSimpleInstruction("ldc", "\"lljvm/lib/c\"");
    printSimpleInstruction("invokevirtual", "lljvm/runtime/Environment/getInstanceByName(Ljava.lang.String;)Llljvm.runtime.CustomLibrary;");
    // stack: ret libc
    printSimpleInstruction("checkcast", "lljvm/lib/c");
    printSimpleInstruction("swap"); // put the return value ahead
    // stack: libc ret
    printSimpleInstruction("invokevirtual", "lljvm/lib/c/exit(I)V");
    printSimpleInstruction("return");
    out << ".end method\n";
}
