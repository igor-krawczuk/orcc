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
package net.sf.orcc.tools.classifier;

import java.util.Map;

import net.sf.orcc.interpreter.InterpretedActor;
import net.sf.orcc.ir.Action;
import net.sf.orcc.ir.Actor;
import net.sf.orcc.ir.Port;

/**
 * This class defines an actor that can be partially interpreted by calling
 * {@link #initialize()} and {@link #schedule()}. It refines the interpreted
 * actor by not relying on anything that is data-dependent.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class PartiallyInterpretedActor extends InterpretedActor {

	private Action scheduledAction;

	/**
	 * Creates a new partially interpreted actor
	 * 
	 * @param id
	 *            instance identifier
	 * @param actor
	 *            an actor
	 */
	public PartiallyInterpretedActor(String id, Actor actor,
			ConfigurationAnalyzer analyzer) {
		super(id, actor);

		// Build a node interpreter for visiting CFG and instructions
		interpret = new PartialNodeInterpreter(id, analyzer);
	}

	@Override
	protected boolean checkOutputPattern(Map<Port, Integer> outputPattern) {
		return true;
	}

	@Override
	protected int execute(Action action) {
		scheduledAction = action;
		return super.execute(action);
	}

	/**
	 * Returns the latest action that was scheduled by the latest call to
	 * {@link #schedule()}.
	 * 
	 * @return the latest scheduled action
	 */
	public Action getScheduledAction() {
		return scheduledAction;
	}

	@Override
	protected boolean isSchedulable(Action action) {
		// no need to check output patterns because we do not have FIFOs
		Object isSchedulable = interpretProc(action.getScheduler());
		return ((isSchedulable instanceof Boolean) && ((Boolean) isSchedulable));
	}

	/**
	 * Sets the configuration action that should be executed.
	 * 
	 * @param action
	 *            an action
	 */
	public void setAction(Action action) {
		((PartialNodeInterpreter) interpret).setAction(action);
	}

}
