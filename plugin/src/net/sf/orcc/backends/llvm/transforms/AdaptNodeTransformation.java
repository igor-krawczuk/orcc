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
package net.sf.orcc.backends.llvm.transforms;

import java.util.List;
import java.util.ListIterator;

import net.sf.orcc.ir.Location;
import net.sf.orcc.ir.VarDef;
import net.sf.orcc.ir.type.IntType;
import net.sf.orcc.ir.actor.Action;
import net.sf.orcc.ir.actor.Actor;
import net.sf.orcc.ir.actor.Procedure;
import net.sf.orcc.ir.actor.VarUse;
import net.sf.orcc.ir.expr.VarExpr;
import net.sf.orcc.ir.nodes.AbstractNode;
import net.sf.orcc.ir.nodes.AbstractNodeVisitor;
import net.sf.orcc.ir.nodes.StoreNode;
import net.sf.orcc.ir.nodes.ReadNode;
import net.sf.orcc.ir.nodes.IfNode;
import net.sf.orcc.ir.nodes.AssignVarNode;
import net.sf.orcc.ir.transforms.ConstantPropagation;
import net.sf.orcc.ir.type.AbstractType;
import net.sf.orcc.backends.llvm.nodes.LoadFifo;
import net.sf.orcc.backends.llvm.nodes.BitcastNode;

/**
 * Move writes to the beginning of an action (because we use pointers).
 * 
 * @author J�r�me GORIN
 * 
 */
public class AdaptNodeTransformation extends AbstractNodeVisitor {
	
	private Procedure procedure;
	
	public AdaptNodeTransformation(Actor actor) {
		for (Procedure proc : actor.getProcs()) {
			visitProc(proc);
		}

		for (Action action : actor.getActions()) {
			visitProc(action.getBody());
			visitProc(action.getScheduler());
		}

		for (Action action : actor.getInitializes()) {
			visitProc(action.getBody());
			visitProc(action.getScheduler());
		}
	}

	@Override
	public void visit(AssignVarNode node, Object... args) {

	}

	@Override
	@SuppressWarnings("unchecked")
	public void visit(ReadNode node, Object... args) {
		ListIterator<AbstractNode> it = (ListIterator<AbstractNode>) args[0];

		VarDef vardef = new  VarDef(false, false, 0, new Location(),
				node.getFifoName()+"_addr", null, null,
				0, new IntType(8));	
		
		
		VarUse varUse = new VarUse(node.getVarDef(), null);
		VarExpr expr = new VarExpr(new Location(), varUse);
		
		LoadFifo loadfifo = new LoadFifo(node.getId(),node.getLocation(), node.getFifoName(), node.getVarDef());
		BitcastNode bitcast = new BitcastNode(node.getId(), node.getLocation(), vardef, expr);
		node.setVar(vardef);
		
		it.previous();
		it.add(loadfifo);
		it.add(bitcast);
		it.next();
	}
	
	@Override
	public void visit(StoreNode node, Object... args) {

	}
	
	@Override
	public void visit(IfNode node, Object... args) {
		visitNodes(node.getThenNodes());
		visitNodes(node.getElseNodes());
		visit(node.getJoinNode(),args);
	}
	
	private void visitNodes(List<AbstractNode> nodes) {
		ListIterator<AbstractNode> it = nodes.listIterator();
		while (it.hasNext()) {
			it.next().accept(this, it);
		}
	}
	
	private void visitProc(Procedure proc) {
		this.procedure = proc;
		visitNodes(proc.getNodes());
	}

}
