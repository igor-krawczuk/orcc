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
 * This class represents a local variable. A local variable is a variable that
 * may have a suffix and an SSA index.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class LocalVariable extends Variable implements
		Comparable<LocalVariable> {

	/**
	 * SSA index.
	 */
	private int index;

	/**
	 * when local variables have the same name but different scopes.
	 */
	private Integer suffix;

	public LocalVariable(boolean assignable, int index, Location loc,
			String name, Integer suffix, Type type) {
		super(loc, type, name, false, assignable);
		this.index = index;
		this.suffix = suffix;
	}

	@Override
	public int compareTo(LocalVariable varDef) {
		return getName().compareTo(varDef.getName());
	}

	/**
	 * Returns the base name of this variable, which is the original name of the
	 * variable, without suffix nor index.
	 * 
	 * @return the base name of this variable
	 */
	public String getBaseName() {
		return super.getName();
	}

	/**
	 * Returns the SSA index of this variable. This information is added when
	 * translating CAL to SSA form.
	 * 
	 * @return the SSA index of this variable
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public String getName() {
		String indexStr = (index == 0) ? "" : "_" + index;
		return getBaseName() + (suffix == null ? "" : suffix) + indexStr;
	}

	/**
	 * Returns the suffix of this variable. This information is used to
	 * disambiguate variables declared with similar names in different scopes.
	 * 
	 * @return the suffix of this variable
	 */
	public int getSuffix() {
		return suffix;
	}

	public boolean hasSuffix() {
		return (suffix != null);
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setSuffix(int suffix) {
		this.suffix = suffix;
	}

}
