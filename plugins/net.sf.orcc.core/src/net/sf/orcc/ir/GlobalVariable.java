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

/**
 * This class represents a global variable. A global variable is a variable that
 * has an initial value in addition to its value as a variable.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class GlobalVariable extends Variable {

	/**
	 * initial value, if present, <code>null</code> otherwise
	 */
	private Expression initialValue;

	/**
	 * Creates a new state variable with the given location, type, name and
	 * initial value expressed as an expression.
	 * 
	 * @param location
	 *            the state variable location
	 * @param type
	 *            the state variable type
	 * @param name
	 *            the state variable name
	 * @param value
	 *            initial value
	 */
	public GlobalVariable(Location location, Type type, String name,
			boolean assignable) {
		this(location, type, name, assignable, null);
	}

	/**
	 * Creates a new state variable with the given location, type, name and
	 * initial value.
	 * 
	 * @param location
	 *            the state variable location
	 * @param type
	 *            the state variable type
	 * @param name
	 *            the state variable name
	 * @param value
	 *            initial value
	 */
	public GlobalVariable(Location location, Type type, String name,
			boolean assignable, Expression initialValue) {
		super(location, type, name, true, assignable);
		this.initialValue = initialValue;
	}

	/**
	 * Returns the initial expression of this variable.
	 * 
	 * @return the initial expression of this variable
	 */
	public Expression getInitialValue() {
		return initialValue;
	}

	/**
	 * Returns <code>true</code> if this state variable has an initial value.
	 * 
	 * @return <code>true</code> if this state variable has an initial value
	 */
	public boolean isInitialized() {
		return (initialValue != null);
	}

	/**
	 * Sets the initial expression of this variable.
	 * 
	 * @param expression
	 *            the initial expression of this variable
	 */
	public void setInitialValue(Expression expression) {
		this.initialValue = expression;
	}

}
