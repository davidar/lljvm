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
 * Return a unique ID.
 * 
 * @return  a unique ID
 */
static uint64_t getUID() {
    static uint64_t x = 0;
    return ++x;
}

/**
 * Replace PHI instructions with copy instructions (load-store pairs).
 * 
 * @param src   the predecessor block
 * @param dest  the destination block
 */
void JVMWriter::printPHICopy(const BasicBlock *src, const BasicBlock *dest) {
    for(BasicBlock::const_iterator i = dest->begin(); isa<PHINode>(i); i++) {
        const PHINode *phi = cast<PHINode>(i);
        const Value *val = phi->getIncomingValueForBlock(src);
        if(isa<UndefValue>(val))
            continue;
        printValueLoad(val);
        printValueStore(phi);
    }
}

/**
 * Print an unconditional branch instruction.
 * 
 * @param curBlock   the current block
 * @param destBlock  the destination block
 */
void JVMWriter::printBranchInstruction(const BasicBlock *curBlock,
                                       const BasicBlock *destBlock) {
    printPHICopy(curBlock, destBlock);
    printSimpleInstruction("goto", getLabelName(destBlock));
}

/**
 * Print a conditional branch instruction.
 * 
 * @param curBlock    the current block
 * @param trueBlock   the destination block if the value on top of the stack is
 *                    non-zero
 * @param falseBlock  the destination block if the value on top of the stack is
 *                    zero
 */
void JVMWriter::printBranchInstruction(const BasicBlock *curBlock,
                                       const BasicBlock *trueBlock,
                                       const BasicBlock *falseBlock) {
    if(trueBlock == falseBlock) {
        printSimpleInstruction("pop");
        printBranchInstruction(curBlock, trueBlock);
    } else if(!falseBlock) {
        printPHICopy(curBlock, trueBlock);
        printSimpleInstruction("ifne", getLabelName(trueBlock));
    } else {
        std::string labelname = getLabelName(trueBlock);
        if(isa<PHINode>(trueBlock->begin()))
            labelname += "$phi" + utostr(getUID());
        printSimpleInstruction("ifne", labelname);
        
        if(isa<PHINode>(falseBlock->begin()))
            printPHICopy(curBlock, falseBlock);
        printSimpleInstruction("goto", getLabelName(falseBlock));
        
        if(isa<PHINode>(trueBlock->begin())) {
            printLabel(labelname);
            printPHICopy(curBlock, trueBlock);
            printSimpleInstruction("goto", getLabelName(trueBlock));
        }
    }
}

/**
 * Print a branch instruction.
 * 
 * @param inst  the branch instrtuction
 */
void JVMWriter::printBranchInstruction(const BranchInst *inst) {
    if(inst->isUnconditional()) {
        printBranchInstruction(inst->getParent(), inst->getSuccessor(0));
    } else {
        printValueLoad(inst->getCondition());
        printBranchInstruction(
            inst->getParent(), inst->getSuccessor(0), inst->getSuccessor(1));
    }
}

/**
 * Print a select instruction.
 * 
 * @param cond      the condition
 * @param trueVal   the return value of the instruction if the condition is
 *                  non-zero
 * @param falseVal  the return value of the instruction if the condition is
 *                  zero
 */
void JVMWriter::printSelectInstruction(const Value *cond,
                                       const Value *trueVal,
                                       const Value *falseVal) {
    std::string labelname = "select" + utostr(getUID());
    printValueLoad(cond);
    printSimpleInstruction("ifeq", labelname + "a");
    printValueLoad(trueVal);
    printSimpleInstruction("goto", labelname + "b");
    printLabel(labelname + "a");
    printValueLoad(falseVal);
    printLabel(labelname + "b");
}

/**
 * Print a switch instruction.
 * 
 * @param inst  the switch instruction
 */
void JVMWriter::printSwitchInstruction(const SwitchInst *inst) {
    // TODO: This method does not handle switch statements when the
    // successor contains phi instructions (the value of the phi instruction
    // should be set before branching to the successor). Therefore, it has
    // been replaced by the switch lowering pass. Once this method is
    // fixed the switch lowering pass should be removed.
    
    std::map<int, unsigned int> cases;
    for(unsigned int i = 1, e = inst->getNumCases(); i < e; i++)
        cases[(int) inst->getCaseValue(i)->getValue().getSExtValue()] = i;
    
    // TODO: tableswitch in cases where it won't increase the size of the
    //       class file
    printValueLoad(inst->getCondition());
    out << "\tlookupswitch\n";
    for(std::map<int, unsigned int>::const_iterator
        i = cases.begin(), e = cases.end(); i != e; i++)
        out << "\t\t" << i->first << " : "
            << getLabelName(inst->getSuccessor(i->second)) << '\n';
    out << "\t\tdefault : " << getLabelName(inst->getDefaultDest()) << '\n';
}

/**
 * Print a loop.
 * 
 * @param l  the loop
 */
void JVMWriter::printLoop(const Loop *l) {
    printLabel(getLabelName(l->getHeader()));
    for(Loop::block_iterator i = l->block_begin(),
                             e = l->block_end(); i != e; i++) {
        const BasicBlock *block = *i;
        Loop *blockLoop = getAnalysis<LoopInfo>().getLoopFor(block);
        if(l == blockLoop)
            // the loop is the innermost parent of this block
            printBasicBlock(block);
        else if(block == blockLoop->getHeader()
                 && l == blockLoop->getParentLoop())
            // this block is the header of its innermost parent loop,
            // and the loop is the parent of that loop
            printLoop(blockLoop);
    }
    printSimpleInstruction("goto", getLabelName(l->getHeader()));
}
