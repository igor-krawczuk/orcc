/*
 * Copyright (c) 2010, Ecole Polytechnique F�d�rale de Lausanne 
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
 *   * Neither the name of the Ecole Polytechnique F�d�rale de Lausanne nor the 
 *     names of its contributors may be used to endorse or promote products 
 *     derived from this software without specific prior written permission.
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

package net.sf.orcc.tools.staticanalyzer;

import java.util.LinkedList;
import java.util.List;

/**
 * This class defines a schedule. A schedule is composed of a header
 * (iterationCount) that defines the number of iteration of the schedule and a
 * body (iterands) that defines the order of invocations of vertex/sub-schedule.
 * 
 * @author Ghislain Roquier
 * 
 */
public class Schedule {

	private int iterationCount;

	private List<Iterand> iterands;

	public Schedule() {
		iterands = new LinkedList<Iterand>();
	}

	public void push(Iterand iterand) {
		iterands.add(iterand);
	}

	public void setIterationCount(int interationCount) {
		this.iterationCount = interationCount;
	}

	public int getIterationCount() {
		return iterationCount;
	}

	public List<Iterand> getIterands() {
		return iterands;
	}

	public String toString() {
		String its = "";
		for (Iterand iterand : iterands)
			its += iterand;
		return "(" + iterationCount + its + ")";
	}
}
