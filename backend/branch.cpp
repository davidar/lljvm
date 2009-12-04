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

static uint64_t getUID() {
    static uint64_t x = 0;
    return ++x;
}

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

void JVMWriter::printBranchToBlock(const BasicBlock *curBlock,
                                   const BasicBlock *trueBlock,
                                   const BasicBlock *falseBlock) {
    if(trueBlock == falseBlock) {
        printPHICopy(curBlock, trueBlock);
        printSimpleInstruction("pop");
        printSimpleInstruction("goto", getLabelName(trueBlock));
    } else if(falseBlock == NULL) {
        printPHICopy(curBlock, trueBlock);
        printSimpleInstruction("ifne", getLabelName(trueBlock));
    } else if(trueBlock == NULL) {
        printPHICopy(curBlock, falseBlock);
        printSimpleInstruction("goto", getLabelName(falseBlock));
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

void JVMWriter::printBranchInstruction(const BranchInst *inst) {
    if(inst->isUnconditional()) {
        printBranchToBlock(inst->getParent(), NULL, inst->getSuccessor(0));
    } else {
        printValueLoad(inst->getCondition());
        printBranchToBlock(inst->getParent(),
                           inst->getSuccessor(0),
                           inst->getSuccessor(1));
    }
}

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

void JVMWriter::printLoop(const Loop *l) {
    printLabel(getLabelName(l->getHeader()));
    const std::vector<BasicBlock*> &blocks = l->getBlocks();
    for(unsigned int i = 0, e = blocks.size(); i < e; i++) {
        BasicBlock *block = blocks[i];
        Loop *blockLoop = getAnalysis<LoopInfo>().getLoopFor(block);
        if(l == blockLoop)
            printBasicBlock(block);
        else if(block == blockLoop->getHeader()
                 && l == blockLoop->getParentLoop())
            printLoop(blockLoop);
    }
    printSimpleInstruction("goto", getLabelName(l->getHeader()));
}
