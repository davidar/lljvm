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

void JVMWriter::printBinaryInstruction(const char *name,
                                       const Value *left,
                                       const Value *right) {
    printValueLoad(left);
    printValueLoad(right);
    out << '\t' << name << '\n';
}

void JVMWriter::printBinaryInstruction(const std::string &name,
                                       const Value *left,
                                       const Value *right) {
    printValueLoad(left);
    printValueLoad(right);
    out << '\t' << name << '\n';
}

void JVMWriter::printSimpleInstruction(const char *inst) {
    out << '\t' << inst << '\n';
}

void JVMWriter::printSimpleInstruction(const char *inst, const char *operand) {
    out << '\t' << inst << ' ' << operand << '\n';
}

void JVMWriter::printSimpleInstruction(const std::string &inst) {
    out << '\t' << inst << '\n';
}

void JVMWriter::printSimpleInstruction(const std::string &inst,
                                       const std::string &operand) {
    out << '\t' << inst << ' ' << operand << '\n';
}

void JVMWriter::printVirtualInstruction(const char *sig) {
    out << '\t' << "invokestatic lljvm/runtime/Instruction/" << sig << '\n';
}

void JVMWriter::printVirtualInstruction(const char *sig,
                                        const Value *operand) {
    printValueLoad(operand);
    printVirtualInstruction(sig);
}

void JVMWriter::printVirtualInstruction(const char *sig,
                                        const Value *left,
                                        const Value *right) {
    printValueLoad(left);
    printValueLoad(right);
    printVirtualInstruction(sig);
}

void JVMWriter::printVirtualInstruction(const std::string &sig) {
    printVirtualInstruction(sig.c_str());
}

void JVMWriter::printVirtualInstruction(const std::string &sig,
                                        const Value *operand) {
    printValueLoad(operand);
    printVirtualInstruction(sig);
}

void JVMWriter::printVirtualInstruction(const std::string &sig,
                                        const Value *left,
                                        const Value *right) {
    printValueLoad(left);
    printValueLoad(right);
    printVirtualInstruction(sig);
}

void JVMWriter::printLabel(const char *label) {
    out << label << ":\n";
}

void JVMWriter::printLabel(const std::string &label) {
    out << label << ":\n";
}
