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
package net.sf.orcc.backends.c;

import java.util.List;

import net.sf.orcc.backends.c.nodes.CNodeVisitor;
import net.sf.orcc.backends.c.nodes.DecrementNode;
import net.sf.orcc.backends.c.nodes.IncrementNode;
import net.sf.orcc.backends.c.nodes.SelfAssignment;
import net.sf.orcc.ir.VarDef;
import net.sf.orcc.ir.expr.IExpr;
import net.sf.orcc.ir.nodes.AbstractNode;
import net.sf.orcc.ir.nodes.AssignVarNode;
import net.sf.orcc.ir.nodes.CallNode;
import net.sf.orcc.ir.nodes.EmptyNode;
import net.sf.orcc.ir.nodes.HasTokensNode;
import net.sf.orcc.ir.nodes.IfNode;
import net.sf.orcc.ir.nodes.InitPortNode;
import net.sf.orcc.ir.nodes.JoinNode;
import net.sf.orcc.ir.nodes.LoadNode;
import net.sf.orcc.ir.nodes.PeekNode;
import net.sf.orcc.ir.nodes.ReadNode;
import net.sf.orcc.ir.nodes.ReturnNode;
import net.sf.orcc.ir.nodes.StoreNode;
import net.sf.orcc.ir.nodes.WhileNode;
import net.sf.orcc.ir.nodes.WriteNode;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

/**
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NodePrinterTemplate implements CNodeVisitor {

	/**
	 * Variable member access switch from private to protected.
	 * 
	 * @see net.sf.orcc.backend.cpp.CppNodePrinter
	 */

	protected String actorName;

	protected String attrName;

	protected ExprToString exprPrinter;

	protected StringTemplateGroup group;

	protected StringTemplate template;

	protected VarDefPrinter varDefPrinter;

	public NodePrinterTemplate(StringTemplateGroup group,
			StringTemplate template, String actorName,
			VarDefPrinter varDefPrinter, ExprToString exprPrinter) {
		attrName = "nodes";
		this.actorName = actorName;
		this.exprPrinter = exprPrinter;
		this.group = group;
		this.template = template;
		this.varDefPrinter = varDefPrinter;
	}

	@Override
	public void visit(AssignVarNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("assignVarNode");

		// varDef contains the variable (with the same name as the port)
		VarDef varDef = node.getVar();
		nodeTmpl.setAttribute("var", varDefPrinter.getVarDefName(varDef));
		nodeTmpl.setAttribute("expr", exprPrinter.toString(node.getValue()));

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(CallNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("callNode");
		if (node.hasRes()) {
			VarDef varDef = node.getRes();
			nodeTmpl.setAttribute("res", varDefPrinter.getVarDefName(varDef));
		}

		nodeTmpl.setAttribute("name", node.getProcedure().getName());
		for (IExpr parameter : node.getParameters()) {
			nodeTmpl
					.setAttribute("parameters", exprPrinter.toString(parameter));
		}

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(DecrementNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("decrementNode");
		VarDef varDef = node.getVar();
		nodeTmpl.setAttribute("var", varDefPrinter.getVarDefName(varDef));

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(EmptyNode node, Object... args) {
		// nothing to print
	}

	@Override
	public void visit(HasTokensNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("hasTokensNode");

		// varDef contains the variable (with the same name as the port)
		VarDef varDef = node.getVarDef();
		nodeTmpl.setAttribute("var", varDefPrinter.getVarDefName(varDef));
		nodeTmpl.setAttribute("actorName", actorName);
		nodeTmpl.setAttribute("fifoName", node.getFifoName());
		nodeTmpl.setAttribute("numTokens", node.getNumTokens());

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(IfNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("ifNode");

		IExpr expr = node.getCondition();
		nodeTmpl.setAttribute("expr", exprPrinter.toString(expr));

		// save current template
		StringTemplate previousTempl = template;
		String previousAttrName = attrName;
		template = nodeTmpl;
		attrName = "thenNodes";

		for (AbstractNode subNode : node.getThenNodes()) {
			subNode.accept(this, args);
		}

		List<AbstractNode> elseNodes = node.getElseNodes();
		if (!(elseNodes.size() == 1 && elseNodes.get(0) instanceof EmptyNode)) {
			attrName = "elseNodes";
			for (AbstractNode subNode : elseNodes) {
				subNode.accept(this, args);
			}
		}

		// restore previous template and attribute name
		attrName = previousAttrName;
		template = previousTempl;
		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(IncrementNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("incrementNode");
		VarDef varDef = node.getVar();
		nodeTmpl.setAttribute("var", varDefPrinter.getVarDefName(varDef));

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(InitPortNode node, Object... args) {

	}

	@Override
	public void visit(JoinNode node, Object... args) {
		// there is nothing to print.
	}

	@Override
	public void visit(LoadNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("loadNode");

		VarDef varDef = node.getTarget();
		nodeTmpl.setAttribute("target", varDefPrinter.getVarDefName(varDef));

		varDef = node.getSource().getVarDef();
		nodeTmpl.setAttribute("source", varDefPrinter.getVarDefName(varDef));

		List<IExpr> indexes = node.getIndexes();
		for (IExpr index : indexes) {
			nodeTmpl.setAttribute("indexes", exprPrinter.toString(index));
		}

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(PeekNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("peekNode");

		// varDef contains the variable (with the same name as the port)
		VarDef varDef = node.getVarDef();
		nodeTmpl.setAttribute("var", varDefPrinter.getVarDefName(varDef));
		nodeTmpl.setAttribute("actorName", actorName);
		nodeTmpl.setAttribute("fifoName", node.getFifoName());
		nodeTmpl.setAttribute("numTokens", node.getNumTokens());

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(ReadNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("readNode");

		// varDef contains the variable (with the same name as the port)
		VarDef varDef = node.getVarDef();
		nodeTmpl.setAttribute("var", varDefPrinter.getVarDefName(varDef));
		nodeTmpl.setAttribute("actorName", actorName);
		nodeTmpl.setAttribute("fifoName", node.getFifoName());
		nodeTmpl.setAttribute("numTokens", node.getNumTokens());

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(ReturnNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("returnNode");
		nodeTmpl.setAttribute("expr", exprPrinter.toString(node.getValue()));
		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(SelfAssignment node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("selfAssignmentNode");

		VarDef varDef = node.getVar();
		nodeTmpl.setAttribute("var", varDefPrinter.getVarDefName(varDef));
		nodeTmpl.setAttribute("op", ExprToString.toString(node.getOp()));
		nodeTmpl.setAttribute("expr", exprPrinter.toString(node.getValue()));

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(StoreNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("storeNode");

		VarDef varDef = node.getTarget().getVarDef();
		nodeTmpl.setAttribute("target", varDefPrinter.getVarDefName(varDef));

		List<IExpr> indexes = node.getIndexes();
		for (IExpr index : indexes) {
			nodeTmpl.setAttribute("indexes", exprPrinter.toString(index));
		}
		nodeTmpl.setAttribute("expr", exprPrinter.toString(node.getValue()));

		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(WhileNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("whileNode");
		IExpr expr = node.getCondition();
		nodeTmpl.setAttribute("expr", exprPrinter.toString(expr));

		// save current template
		StringTemplate previousTempl = template;
		String previousAttrName = attrName;
		template = nodeTmpl;
		attrName = "nodes";

		for (AbstractNode subNode : node.getNodes()) {
			subNode.accept(this, args);
		}

		// restore previous template
		attrName = previousAttrName;
		template = previousTempl;
		template.setAttribute(attrName, nodeTmpl);
	}

	@Override
	public void visit(WriteNode node, Object... args) {
		StringTemplate nodeTmpl = group.getInstanceOf("writeNode");

		// varDef contains the variable (with the same name as the port)
		VarDef varDef = node.getVarDef();
		nodeTmpl.setAttribute("var", varDefPrinter.getVarDefName(varDef));
		nodeTmpl.setAttribute("actorName", actorName);
		nodeTmpl.setAttribute("fifoName", node.getFifoName());
		nodeTmpl.setAttribute("numTokens", node.getNumTokens());

		template.setAttribute(attrName, nodeTmpl);
	}

}
