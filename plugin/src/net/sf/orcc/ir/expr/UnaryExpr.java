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
package net.sf.orcc.ir.expr;

import net.sf.orcc.ir.Expression;
import net.sf.orcc.ir.Location;
import net.sf.orcc.ir.Type;

/**
 * This class defines a unary expression.
 * 
 * @author Matthieu Wipliez
 * @author J�r�me Gorin
 * 
 */
public class UnaryExpr extends AbstractExpression {

	private Expression expr;

	private UnaryOp op;

	private Type type;

	public UnaryExpr(Location location, UnaryOp op, Expression expr, Type type) {
		super(location);
		this.expr = expr;
		this.op = op;
		this.type = type;
	}

	public UnaryExpr(UnaryOp op, Expression expr, Type type) {
		this(new Location(), op, expr, type);
	}

	@Override
	public Object accept(ExpressionInterpreter interpreter, Object... args) {
		return interpreter.interpret(this, args);
	}

	@Override
	public void accept(ExpressionVisitor visitor, Object... args) {
		visitor.visit(this, args);
	}

	public Expression getExpr() {
		return expr;
	}

	public UnaryOp getOp() {
		return op;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public int getTypeOf() {
		return UNARY;
	}

	public void setExpr(Expression expr) {
		this.expr = expr;
	}

	public void setOp(UnaryOp op) {
		this.op = op;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
