/*
 * Copyright (c) 2009, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package net.sf.orcc.ir;

import net.sf.orcc.ir.instructions.InstructionInterpreter;
import net.sf.orcc.ir.instructions.InstructionVisitor;
import net.sf.orcc.ir.nodes.BlockNode;

/**
 * This class defines an instruction.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface Instruction extends User {

	/**
	 * Accepts the given instruction interpreter.
	 * 
	 * @param interpreter
	 *            an interpreter
	 * @param args
	 *            arguments
	 * @return an object
	 */
	public Object accept(InstructionInterpreter interpreter, Object... args);

	/**
	 * Accepts the given instruction visitor.
	 * 
	 * @param visitor
	 *            a visitor
	 * @param args
	 *            arguments
	 */
	public void accept(InstructionVisitor visitor, Object... args);

	/**
	 * Returns the block that contains this instruction.
	 * 
	 * @return the block that contains this instruction
	 */
	public BlockNode getBlock();

	/**
	 * Returns instruction casting type if needed.
	 * 
	 * @return Type of cast made by this instruction
	 */
	public Type getCast();
	
	/**
	 * Sets the block that contains this instruction.
	 * 
	 * @param block
	 *            the block that contains this instruction
	 */
	public void setBlock(BlockNode block);

}
