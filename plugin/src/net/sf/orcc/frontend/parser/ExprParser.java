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
package net.sf.orcc.frontend.parser;

import static net.sf.orcc.frontend.parser.Util.parseLocation;

import java.util.ArrayList;
import java.util.List;

import net.sf.orcc.OrccException;
import net.sf.orcc.frontend.parser.internal.ALBaseLexer;
import net.sf.orcc.ir.IExpr;
import net.sf.orcc.ir.Use;
import net.sf.orcc.ir.expr.BinaryOp;
import net.sf.orcc.ir.expr.BooleanExpr;
import net.sf.orcc.ir.expr.IntExpr;
import net.sf.orcc.ir.expr.StringExpr;
import net.sf.orcc.ir.expr.VarExpr;
import net.sf.orcc.util.BinOpSeqParser;

import org.antlr.runtime.tree.Tree;

/**
 * This class defines a parser that can parse RVC-CAL expressions and translate
 * them to IR expressions.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ExprParser {

	/**
	 * Returns the binary operator that match the type of the given tree.
	 * 
	 * @param op
	 *            a Tree that represents an operator
	 * @return a binary operator
	 * @throws OrccException
	 *             if the operator is not valid
	 */
	private BinaryOp parseBinaryOp(Tree op) throws OrccException {
		switch (op.getType()) {
		case ALBaseLexer.LOGIC_AND:
			return BinaryOp.LOGIC_AND;
		case ALBaseLexer.BITAND:
			return BinaryOp.BITAND;
		case ALBaseLexer.BITOR:
			return BinaryOp.BITOR;
		case ALBaseLexer.DIV:
			return BinaryOp.DIV;
		case ALBaseLexer.DIV_INT:
			return BinaryOp.DIV_INT;
		case ALBaseLexer.EQ:
			return BinaryOp.EQ;
		case ALBaseLexer.EXP:
			return BinaryOp.EXP;
		case ALBaseLexer.GE:
			return BinaryOp.GE;
		case ALBaseLexer.GT:
			return BinaryOp.GT;
		case ALBaseLexer.LE:
			return BinaryOp.LE;
		case ALBaseLexer.LT:
			return BinaryOp.LT;
		case ALBaseLexer.MINUS:
			return BinaryOp.MINUS;
		case ALBaseLexer.MOD:
			return BinaryOp.MOD;
		case ALBaseLexer.NE:
			return BinaryOp.NE;
		case ALBaseLexer.LOGIC_OR:
			return BinaryOp.LOGIC_OR;
		case ALBaseLexer.PLUS:
			return BinaryOp.PLUS;
		case ALBaseLexer.SHIFT_LEFT:
			return BinaryOp.SHIFT_LEFT;
		case ALBaseLexer.SHIFT_RIGHT:
			return BinaryOp.SHIFT_RIGHT;
		case ALBaseLexer.TIMES:
			return BinaryOp.TIMES;
		default:
			throw new OrccException("Unknown operator: " + op.getText());
		}
	}

	/**
	 * Parses a sequence of binary operations represented by an ANTLR tree, and
	 * transforms it to a binary expression tree using the operators'
	 * precedences.
	 * 
	 * @param expr
	 *            a tree that contains a binary operation sequence
	 * @return an expression
	 * @throws OrccException
	 */
	private IExpr parseBinOpSeq(Tree expr) throws OrccException {
		Tree treeExprs = expr.getChild(0);
		int numExprs = treeExprs.getChildCount();
		List<IExpr> expressions = new ArrayList<IExpr>(numExprs);
		for (int i = 0; i < numExprs; i++) {
			expressions.add(parseExpression(treeExprs.getChild(i)));
		}

		Tree treeOps = expr.getChild(1);
		int numOps = treeOps.getChildCount();
		List<BinaryOp> operators = new ArrayList<BinaryOp>(numOps);
		for (int i = 0; i < numOps; i++) {
			operators.add(parseBinaryOp(treeOps.getChild(i)));
		}

		return BinOpSeqParser.parse(expressions, operators);
	}

	/**
	 * Parses the given tree as an expression. This method is package because it
	 * should be called from {@link RVCCalASTParser} only.
	 * 
	 * @param expr
	 *            a tree that contains an expression
	 * @return an {@link IExpr}.
	 * @throws OrccException
	 */
	IExpr parseExpression(Tree expr) throws OrccException {
		switch (expr.getType()) {
		case ALBaseLexer.EXPR_BINARY:
			return parseBinOpSeq(expr);
		case ALBaseLexer.EXPR_BOOL: {
			expr = expr.getChild(0);
			boolean value = Boolean.parseBoolean(expr.getText());
			return new BooleanExpr(parseLocation(expr), value);
		}
		case ALBaseLexer.EXPR_FLOAT:
			throw new OrccException("not yet implemented!");
		case ALBaseLexer.EXPR_INT:
			expr = expr.getChild(0);
			int value = Integer.parseInt(expr.getText());
			return new IntExpr(parseLocation(expr), value);
		case ALBaseLexer.EXPR_STRING:
			expr = expr.getChild(0);
			return new StringExpr(parseLocation(expr), expr.getText());
		case ALBaseLexer.EXPR_VAR:
			expr = expr.getChild(0);
			Use localUse = null;
			return new VarExpr(parseLocation(expr), localUse);
		default:
			throw new OrccException("not yet implemented");
		}
	}

}
