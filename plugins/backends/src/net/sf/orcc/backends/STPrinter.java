/*
 * Copyright (c) 2010, IETR/INSA of Rennes
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
package net.sf.orcc.backends;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import net.sf.orcc.OrccRuntimeException;
import net.sf.orcc.ir.Actor;
import net.sf.orcc.ir.Expression;
import net.sf.orcc.ir.Type;
import net.sf.orcc.ir.expr.BinaryExpr;
import net.sf.orcc.ir.expr.BoolExpr;
import net.sf.orcc.ir.expr.ExpressionPrinter;
import net.sf.orcc.ir.expr.IntExpr;
import net.sf.orcc.ir.expr.ListExpr;
import net.sf.orcc.ir.expr.StringExpr;
import net.sf.orcc.ir.expr.UnaryExpr;
import net.sf.orcc.ir.expr.VarExpr;
import net.sf.orcc.ir.type.BoolType;
import net.sf.orcc.ir.type.FloatType;
import net.sf.orcc.ir.type.IntType;
import net.sf.orcc.ir.type.ListType;
import net.sf.orcc.ir.type.StringType;
import net.sf.orcc.ir.type.TypePrinter;
import net.sf.orcc.ir.type.UintType;
import net.sf.orcc.ir.type.VoidType;
import net.sf.orcc.network.Instance;
import net.sf.orcc.network.Network;

import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.debug.DebugST;

/**
 * This class defines a printer that uses StringTemplate.
 * 
 * @author Matthieu Wipliez
 * 
 */
public final class STPrinter {

	private class ExpressionRenderer implements AttributeRenderer {

		@Override
		public String toString(Object o, String formatString, Locale locale) {
			return STPrinter.this.toString((Expression) o);
		}

	}

	private class StringRenderer implements AttributeRenderer {

		@Override
		public String toString(Object o, String formatString, Locale locale) {
			// only calls toString when format is "constant"
			// first tests for null because it is faster
			if (formatString != null && "constant".equals(formatString)) {
				return STPrinter.this.toString((String) o);
			} else {
				return (String) o;
			}
		}

	}

	private class TypeRenderer implements AttributeRenderer {

		@Override
		public String toString(Object o, String formatString, Locale locale) {
			return STPrinter.this.toString((Type) o);
		}

	}

	private Class<? extends ExpressionPrinter> expressionPrinter;

	final protected STGroup group;

	private Class<? extends TypePrinter> typePrinter;

	/**
	 * Creates a new StringTemplate printer with the given template group name.
	 * 
	 * @param groupNames
	 *            names of the template groups
	 * @throws IOException
	 *             If the template file could not be read.
	 */
	public STPrinter(String... groupNames) {
		group = TemplateGroupLoader.loadGroup(groupNames);

		// set to "true" to inspect template
		group.debug = false;

		// register renderers
		group.registerRenderer(String.class, new StringRenderer());

		AttributeRenderer renderer;

		Class<?>[] classesExpression = { BinaryExpr.class, BoolExpr.class,
				IntExpr.class, ListExpr.class, StringExpr.class,
				UnaryExpr.class, VarExpr.class };
		renderer = new ExpressionRenderer();
		for (Class<?> clasz : classesExpression) {
			group.registerRenderer(clasz, renderer);
		}

		Class<?>[] classesType = { BoolType.class, FloatType.class,
				IntType.class, ListType.class, StringType.class,
				UintType.class, VoidType.class };
		renderer = new TypeRenderer();
		for (Class<?> clasz : classesType) {
			group.registerRenderer(clasz, renderer);
		}
	}

	/**
	 * Prints the given actor to a file whose name is given.
	 * 
	 * @param fileName
	 *            output file name
	 * @param actor
	 *            the actor
	 * @throws IOException
	 */
	public void printActor(String fileName, Actor actor) throws IOException {
		if (!actor.isSystem()) {
			if (group.debug) {
				DebugST template = (DebugST) group.getInstanceOf("actor");
				template.add("actor", actor);
				template.inspect();
			} else {
				ST template = group.getInstanceOf("actor");
				template.add("actor", actor);

				byte[] b = template.render(80).getBytes();
				OutputStream os = new FileOutputStream(fileName);
				os.write(b);
				os.close();
			}
		}
	}

	/**
	 * Prints the given instance to a file whose name is given.
	 * 
	 * @param fileName
	 *            output file name
	 * @param instance
	 *            the instance
	 * @throws IOException
	 */
	public void printInstance(String fileName, Instance instance)
			throws IOException {
		if (!instance.isActor() || !instance.getActor().isSystem()) {
			ST template = group.getInstanceOf("instance");

			template.add("instance", instance);

			byte[] b = template.render(80).getBytes();
			OutputStream os = new FileOutputStream(fileName);
			os.write(b);
			os.close();
		}
	}

	/**
	 * Prints the given network to a file whose name is given. debugFifos
	 * specifies whether debug information should be printed about FIFOs, and
	 * fifoSize is the default FIFO size.
	 * 
	 * @param fileName
	 *            The output file name.
	 * @param network
	 *            The network to generate code for.
	 * @param debugFifos
	 *            Whether debug information should be printed about FIFOs.
	 * @param fifoSize
	 *            Default FIFO size.
	 * @throws IOException
	 *             if there is an I/O error
	 */
	public void printNetwork(String fileName, Network network,
			boolean debugFifos, int fifoSize) throws IOException {
		ST template = group.getInstanceOf("network");

		network.computeTemplateMaps();

		template.add("debugFifos", debugFifos);
		template.add("network", network);
		template.add("fifoSize", fifoSize);

		byte[] b = template.render(80).getBytes();
		OutputStream os = new FileOutputStream(fileName);
		os.write(b);
		os.close();
	}

	public void setExpressionPrinter(Class<? extends ExpressionPrinter> printer) {
		this.expressionPrinter = printer;
	}

	public void setTypePrinter(Class<? extends TypePrinter> printer) {
		this.typePrinter = printer;
	}

	private String toString(Expression expression) {
		ExpressionPrinter printer;
		try {
			printer = expressionPrinter.newInstance();
		} catch (InstantiationException e) {
			throw new OrccRuntimeException(
					"expression printer cannot be instantiated", e);
		} catch (IllegalAccessException e) {
			throw new OrccRuntimeException(
					"expression printer cannot be instantiated", e);
		}
		expression.accept(printer, Integer.MAX_VALUE);
		return printer.toString();
	}

	private String toString(String string) {
		StringBuilder builder = new StringBuilder();
		builder.append('"');
		builder.append(string.replaceAll("\\\\", "\\\\\\\\"));
		builder.append('"');
		return builder.toString();
	}

	private String toString(Type type) {
		TypePrinter printer;
		try {
			printer = typePrinter.newInstance();
		} catch (InstantiationException e) {
			throw new OrccRuntimeException(
					"type printer cannot be instantiated", e);
		} catch (IllegalAccessException e) {
			throw new OrccRuntimeException(
					"type printer cannot be instantiated", e);
		}
		type.accept(printer);
		return printer.toString();
	}

}
