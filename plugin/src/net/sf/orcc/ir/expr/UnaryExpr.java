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

import net.sf.orcc.ir.Location;
import net.sf.orcc.ir.type.AbstractType;

/**
 * @author Matthieu Wipliez
 * 
 */
public class UnaryExpr extends AbstractExpr {

	private IExpr expr;

	private UnaryOp op;

	private AbstractType type;

	public UnaryExpr(Location location, UnaryOp op, IExpr expr,
			AbstractType type) {
		super(location);
		this.expr = expr;
		this.op = op;
		this.type = type;
	}

	@Override
	public void accept(ExprVisitor visitor, Object... args) {
		visitor.visit(this, args);
	}

	@Override
	public IExpr evaluate() throws ExprEvaluateException {
		switch (op) {
		case BNOT:
			break;
		case LNOT:
			break;
		case MINUS:
			break;
		case NUM_ELTS:
			break;
		}

		throw new ExprEvaluateException("could not evaluate");
	}

	public IExpr getExpr() {
		return expr;
	}

	@Override
	public int getExprType() {
		return UNARY;
	}

	public UnaryOp getOp() {
		return op;
	}

	public AbstractType getType() {
		return type;
	}

	public void setExpr(IExpr expr) {
		this.expr = expr;
	}

	public void setOp(UnaryOp op) {
		this.op = op;
	}

	public void setType(AbstractType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return op.toString() + expr;
	}

}
