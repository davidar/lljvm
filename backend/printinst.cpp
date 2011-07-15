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

namespace {
const char TAG_INVOKE_BEGIN[] = "*LLJVM|INVOKE-BEGIN";
const char TAG_INVOKE_END[] = "*LLJVM|INVOKE-END";
const char TAG_GET_FIELD[] = "*LLJVM|GET-FIELD";
const char TAG_CLASSNAME_FOR_METHOD[] = "*LLJVM|CLASSNAME-FOR-METHOD";
const char TAG_LINKER_DECLARATIONS[] = "*LLJVM|LINKER-DECLARATIONS";
const char TAG_LINKER_INITIALIZATIONS[] = "*LLJVM|LINKER-INITIALIZATIONS";
}

/**
 * Print the given binary instruction.
 * 
 * @param name   the name of the instruction
 * @param left   the first operand
 * @param right  the second operand
 */
void JVMWriter::printBinaryInstruction(const char *name,
                                       const Value *left,
                                       const Value *right) {
    printValueLoad(left);
    printValueLoad(right);
    out << '\t' << name << '\n';
}

/**
 * Print the given binary instruction.
 * 
 * @param name   the name of the instruction
 * @param left   the first operand
 * @param right  the second operand
 */
void JVMWriter::printBinaryInstruction(const std::string &name,
                                       const Value *left,
                                       const Value *right) {
    printValueLoad(left);
    printValueLoad(right);
    out << '\t' << name << '\n';
}

/**
 * Print the given instruction.
 * 
 * @param inst  the instruction
 */
void JVMWriter::printSimpleInstruction(const char *inst) {
    out << '\t' << inst << '\n';
}

/**
 * Print the given instruction.
 * 
 * @param inst     the instruction
 * @param operand  the operand to the instruction
 */
void JVMWriter::printSimpleInstruction(const char *inst, const char *operand) {
    out << '\t' << inst << ' ' << operand << '\n';
}

/**
 * Print the given instruction.
 * 
 * @param inst  the instruction
 */
void JVMWriter::printSimpleInstruction(const std::string &inst) {
    out << '\t' << inst << '\n';
}

/**
 * Print the given instruction.
 * 
 * @param inst     the instruction
 * @param operand  the operand to the instruction
 */
void JVMWriter::printSimpleInstruction(const std::string &inst,
                                       const std::string &operand) {
    out << '\t' << inst << ' ' << operand << '\n';
}

/**
 * Print the virtual instruction with the given signature.
 * 
 * @param sig  the signature of the instruction
 */
void JVMWriter::printVirtualInstruction(const char *sig) {
    out << '\t' << "invokestatic lljvm/runtime/Instruction/" << sig << '\n';
}

/**
 * Print the virtual instruction with the given signature.
 * 
 * @param sig      the signature of the instruction
 * @param operand  the operand to the instruction
 */
void JVMWriter::printVirtualInstruction(const char *sig,
                                        const Value *operand) {
    printValueLoad(operand);
    printVirtualInstruction(sig);
}

/**
 * Print the virtual instruction with the given signature.
 * 
 * @param sig    the signature of the instruction
 * @param left   the first operand
 * @param right  the second operand
 */
void JVMWriter::printVirtualInstruction(const char *sig,
                                        const Value *left,
                                        const Value *right) {
    printValueLoad(left);
    printValueLoad(right);
    printVirtualInstruction(sig);
}

/**
 * Print the virtual instruction with the given signature.
 * 
 * @param sig  the signature of the instruction
 */
void JVMWriter::printVirtualInstruction(const std::string &sig) {
    printVirtualInstruction(sig.c_str());
}

/**
 * Print the virtual instruction with the given signature.
 * 
 * @param sig      the signature of the instruction
 * @param operand  the operand to the instruction
 */
void JVMWriter::printVirtualInstruction(const std::string &sig,
                                        const Value *operand) {
    printValueLoad(operand);
    printVirtualInstruction(sig);
}

/**
 * Print the virtual instruction with the given signature.
 * 
 * @param sig    the signature of the instruction
 * @param left   the first operand
 * @param right  the second operand
 */
void JVMWriter::printVirtualInstruction(const std::string &sig,
                                        const Value *left,
                                        const Value *right) {
    printValueLoad(left);
    printValueLoad(right);
    printVirtualInstruction(sig);
}

/**
 * Print the given label.
 * 
 * @param label  the label
 */
void JVMWriter::printLabel(const char *label) {
    out << label << ":\n";
}

/**
 * Print the given label.
 * 
 * @param label  the label
 */
void JVMWriter::printLabel(const std::string &label) {
    out << label << ":\n";
}

/**
 * Print the linker tag indicating the start of a JVM invocation sequence.
 * This should be followed by instructions placing the invocation arguments
 * on the stack and finally by an end invocation tag.
 *
 * @param includeStackSize number of existing positions on the stack that should
 * be included in the invocation.  For non-static invocations, the linker-generated
 * code must insert a reference to the target instance below these items.  Currently
 * only 0 and 1 are supported.
 */
void JVMWriter::printStartInvocationTag(int includeStackSize) {
    out << TAG_INVOKE_BEGIN << "|includeStackSize=" << includeStackSize << "\n";
}

/**
 * Print a linker tag indicating the end of a JVM invocation sequence.
 * @param sig a symbolic reference to the method to invoke.  The reference may be
 * unqualified, in which case the target classname will be resolved by the linker.
 * @param local if true, then the call is to this instance rather than an external instance.
 */
void JVMWriter::printEndInvocationTag(const std::string &sig, bool local) {
    out << TAG_INVOKE_END << "|sig=" << sig << "|local=" << (local?"true":"false") << "\n";
}

/**
 * Print a linker tag for generating a field access.
 * @param sig a symbolic reference to the field.  The reference may be
 * unqualified, in which case the target classname will be resolved by the linker.
 * @param local if true, then the access is to this instance rather than an external instance.
 */
void JVMWriter::printGetField(const std::string &sig, bool local) {
    out << TAG_GET_FIELD << "|sig=" << sig << "|local=" << (local?"true":"false") << "\n";
}

/**
 * Print a linker tag for loading a resolved class name on the stack.  At link time,
 * this generates a "ldc" instruction for the resolved class name.
 * @param sig a symbolic reference to the method to invoke.  The reference may be
 * unqualified, in which case the target classname will be resolved by the linker.
 */
void JVMWriter::printLoadClassNameForMethod(const std::string &sig) {
    out << TAG_CLASSNAME_FOR_METHOD << "|sig=" << sig << "\n";
}

/**
 * Print the linker declarations placeholder.   The linker will replace this with
 * field declarations for the members it creates.
 */
void JVMWriter::printDeclareLinkerFields() {
    out << TAG_LINKER_DECLARATIONS << "\n";
}

/**
 * Print the linker initializations placeholder.  The linker will replace this
 * with code for initializing the fields it defined.
 */
void JVMWriter::printInitLinkerFields() {
    out << TAG_LINKER_INITIALIZATIONS << "\n";
}

void JVMWriter::printTrc(const std::string &sig) {
    if (!trace)
        return;
    (*trace) << sig << '\n';
    trcLineNum++;
    std::string::size_type pos;
    for(pos=0;(pos=sig.find('\n',pos))!=std::string::npos;pos++)
        trcLineNum++;
    
}

