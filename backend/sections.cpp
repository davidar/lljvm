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
    printLinkerHeader();
    out << ".bytecode 49.0\n";
    if(debug >= 1)
        out << ".source " << sourcename << "\n";
    out << ".class public " << classname << "\n"
           ".super java/lang/Object\n\n" << "\n"
           ".implements lljvm/runtime/Module" << "\n";
    out << "; ;;START LINKER DECLARATIONS;; ;\n";
    printDeclareLinkerFields();
    out << "; ;;END LINKER DECLARATIONS;; ;\n\n";
}

/**
 * Print the field declarations.
 */
void JVMWriter::printFields() {
    out << "; Fields\n";

    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(i->isDeclaration()) {
            out << ";.extern field ";
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
            out << ";.extern method "
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
           ".method public <init>()V\n";
    printSimpleInstruction("aload_0");
    printSimpleInstruction("invokespecial","java/lang/Object/<init>()V");
    printSimpleInstruction("return");
    printSimpleInstruction(".limit stack 1");
    printSimpleInstruction(".limit locals 1");
    out << ".end method\n\n";
    
    out << ".method public initialize(Llljvm.runtime.Context;)V\n";

    printSimpleInstruction(".limit stack 12");
    printSimpleInstruction(".limit locals 2");

    out << "\n;;;START LINKER INITIALIZATIONS;;;\n";
    printInitLinkerFields();
    out << ";;;END LINKER INITIALIZATIONS;;;\n\n";

    
    out << "\n\t; allocate global variables\n";

    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(!i->isDeclaration()) {
            const GlobalVariable *g = i;
            const Constant *c = g->getInitializer();
            printSimpleInstruction("aload_0");
            printStartInvocationTag();
            printConstLoad(
                APInt(32, targetData->getTypeAllocSize(c->getType()), false));
            printEndInvocationTag("lljvm/runtime/Memory/allocateData(I)I");
            printSimpleInstruction("putfield",
                classname + "/" + getValueName(g) + " I");
        }
    }
    
    out << "\n\t; initialize global variables\n";
    for(Module::global_iterator i = module->global_begin(),
                                e = module->global_end(); i != e; i++) {
        if(!i->isDeclaration()) {
            const GlobalVariable *g = i;
            const Constant *c = g->getInitializer();
            printSimpleInstruction("aload_0");
            printSimpleInstruction("getfield",
                classname + "/" + getValueName(g) + " I");
            printStaticConstant(c);
            printSimpleInstruction("pop");
            out << '\n';
        }
    }

    out << "\n"
           "\treturn\n"
           ".end method\n\n";
           
           
   out << ".method public destroy(Llljvm.runtime.Context;)V\n";
   printSimpleInstruction("return");
   printSimpleInstruction(".limit stack 0");
   printSimpleInstruction(".limit locals 2");   
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
    printSimpleInstruction(".limit stack 6");
    printSimpleInstruction(".limit locals 2");
    printSimpleInstruction("new","lljvm/runtime/DefaultContext");
    printSimpleInstruction("dup");
    printSimpleInstruction("invokespecial","lljvm/runtime/DefaultContext/<init>()V");
    printSimpleInstruction("dup");
    printSimpleInstruction("astore_1");
    printSimpleInstruction("ldc",classname);
    printSimpleInstruction("invokeinterface","lljvm/runtime/Context/getModule(Ljava/lang/Class;)Ljava/lang/Object; 2");
    printSimpleInstruction("checkcast",classname);
    if(f->arg_size() == 0) {
        printSimpleInstruction("invokevirtual",classname + "/main()I");
    } else if(f->arg_size() == 2) {
        Function::const_arg_iterator arg1, arg2;
        arg1 = arg2 = f->arg_begin(); arg2++;
        if(!arg1->getType()->isIntegerTy()
         || arg2->getType()->getTypeID() != Type::PointerTyID)
            llvm_unreachable("main function has invalid type signature");
        printSimpleInstruction("aload_0");
        printSimpleInstruction("arraylength");

        printSimpleInstruction("aload_1");
        printSimpleInstruction("ldc","lljvm/runtime/Memory");
        printSimpleInstruction("invokeinterface","lljvm/runtime/Context/getModule(Ljava/lang/Class;)Ljava/lang/Object; 2");
        printSimpleInstruction("checkcast","lljvm/runtime/Memory");
        printSimpleInstruction("aload_0");       
        printSimpleInstruction("invokevirtual",
            "lljvm/runtime/Memory/storeStack([Ljava/lang/String;)I");
            
        printSimpleInstruction("invokevirtual", classname + "/main("
            + getTypeDescriptor(arg1->getType())
            + getTypeDescriptor(arg2->getType()) + ")I");
    } else {
        llvm_unreachable("main function has invalid number of arguments");
    }

    printSimpleInstruction("aload_1");
    printSimpleInstruction("ldc","lljvm/lib/c");
    printSimpleInstruction("invokeinterface","lljvm/runtime/Context/getModule(Ljava/lang/Class;)Ljava/lang/Object; 2");
    printSimpleInstruction("checkcast","lljvm/lib/c");
    printSimpleInstruction("swap");
    printSimpleInstruction("invokevirtual", "lljvm/lib/c/exit(I)V");
    printSimpleInstruction("return");
    out << ".end method\n";
}

