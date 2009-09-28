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
package net.sf.orcc.ir.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.orcc.ir.Location;
import net.sf.orcc.ir.VarDef;
import net.sf.orcc.ir.actor.Actor;
import net.sf.orcc.ir.expr.IExpr;
import net.sf.orcc.ir.expr.BooleanExpr;
import net.sf.orcc.ir.expr.IntExpr;
import net.sf.orcc.ir.expr.ListExpr;
import net.sf.orcc.ir.expr.StringExpr;
import net.sf.orcc.ir.network.Connection;
import net.sf.orcc.ir.network.Instance;
import net.sf.orcc.ir.network.Network;
import net.sf.orcc.ir.type.AbstractType;

import org.jgrapht.graph.DirectedMultigraph;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

/**
 * @author Matthieu Wipliez
 * 
 */
public class NetworkParser {

	private Map<String, Instance> instances;

	private String path;

	private void checkConnections(DirectedMultigraph<Instance, Connection> graph) {
		Set<Connection> connections = graph.edgeSet();
		for (Connection connection : connections) {
			Instance source = graph.getEdgeSource(connection);
			Instance target = graph.getEdgeTarget(connection);
			Actor srcActor = source.getActor();
			Actor tgtActor = target.getActor();

			AbstractType srcType = connection.getSource().getType();
			AbstractType dstType = connection.getTarget().getType();
			if (!srcType.equals(dstType)) {
				throw new IllegalArgumentException("Type error: " + srcActor
						+ "." + connection.getSource() + " is " + srcType
						+ ", " + tgtActor + "." + connection.getTarget()
						+ " is " + dstType);
			}
		}
	}

	private void checkInstances(Instance source, String src, Instance target,
			String dst) {
		if (source == null) {
			throw new NetworkParseException("A Connection refers to "
					+ "a non-existent Instance: \"" + src + "\"");
		}
		if (target == null) {
			throw new NetworkParseException("A Connection refers to "
					+ "a non-existent Instance: \"" + dst + "\"");
		}
	}

	private void checkPorts(String src, String src_port, String dst,
			String dst_port) {
		if (src.isEmpty()) {
			throw new NetworkParseException("A Connection element "
					+ "must have a valid non-empty \"src\" attribute");
		} else if (src_port.isEmpty()) {
			throw new NetworkParseException("An Connection element "
					+ "must have a valid non-empty \"src-port\" " + "attribute");
		} else if (dst.isEmpty()) {
			throw new NetworkParseException("An Connection element "
					+ "must have a valid non-empty \"dst\" attribute");
		} else if (dst_port.isEmpty()) {
			throw new NetworkParseException("An Connection element "
					+ "must have a valid non-empty \"dst-port\" " + "attribute");
		}
	}

	private void checkPortsVarDef(VarDef srcPort, String src_port,
			VarDef dstPort, String dst_port) {
		if (srcPort == null) {
			throw new NetworkParseException("A Connection refers to "
					+ "a non-existent source port: \"" + src_port + "\"");
		}
		if (dstPort == null) {
			throw new NetworkParseException("A Connection refers to "
					+ "a non-existent target port: \"" + dst_port + "\"");
		}
	}

	private void parseConnections(
			DirectedMultigraph<Instance, Connection> graph, Node node) {
		while (node != null) {
			if (node.getNodeName().equals("Connection")) {
				Element connection = (Element) node;
				String src = connection.getAttribute("src");
				String src_port = connection.getAttribute("src-port");
				String dst = connection.getAttribute("dst");
				String dst_port = connection.getAttribute("dst-port");

				checkPorts(src, src_port, dst, dst_port);

				Instance source = instances.get(src);
				Instance target = instances.get(dst);

				checkInstances(source, src, target, dst);

				VarDef srcPort = source.getActor().getOutput(src_port);
				VarDef dstPort = target.getActor().getInput(dst_port);

				checkPortsVarDef(srcPort, src_port, dstPort, dst_port);

				Integer size = parseSize(connection.getFirstChild());

				Connection conn = new Connection(srcPort, dstPort, size);
				graph.addEdge(source, target, conn);
			}

			node = node.getNextSibling();
		}
	}

	private IExpr parseExpr(Node node) {
		while (node != null) {
			if (node.getNodeName().equals("Expr")) {
				Element elt = (Element) node;
				String kind = elt.getAttribute("kind");
				if (kind.equals("Literal")) {
					kind = elt.getAttribute("literal-kind");
					String value = elt.getAttribute("value");
					if (kind.equals("Boolean")) {
						return new BooleanExpr(new Location(), Boolean
								.parseBoolean(value));
					} else if (kind.equals("Integer")) {
						return new IntExpr(new Location(), Integer
								.parseInt(value));
					} else if (kind.equals("String")) {
						return new StringExpr(new Location(), value);
					} else {
						throw new NetworkParseException("Unsupported Expr "
								+ "literal kind: \"" + kind + "\"");
					}
				} else if (kind.equals("List")) {
					List<IExpr> exprs = parseExprs(node.getFirstChild());
					return new ListExpr(new Location(), exprs);
				} else {
					throw new NetworkParseException("Unsupported Expr kind: \""
							+ kind + "\"");
				}
			}

			node = node.getNextSibling();
		}

		throw new NetworkParseException("Expected a Expr element");
	}

	private List<IExpr> parseExprs(Node node) {
		List<IExpr> exprs = new ArrayList<IExpr>();
		while (node != null) {
			if (node.getNodeName().equals("Expr")) {
				exprs.add(parseExpr(node));
			}

			node = node.getNextSibling();
		}

		return exprs;
	}

	private DirectedMultigraph<Instance, Connection> parseGraph(Element root) {
		DirectedMultigraph<Instance, Connection> graph = new DirectedMultigraph<Instance, Connection>(
				Connection.class);

		Node node = parseInstances(graph, root);

		if (instances.isEmpty()) {
			throw new NetworkParseException(
					"A valid network must contain at least one instance");
		}

		parseConnections(graph, node);

		return graph;
	}

	private Instance parseInstance(Node node) {
		// instance id
		String id = ((Element) node).getAttribute("id");
		if (id.isEmpty()) {
			throw new NetworkParseException("An Instance element "
					+ "must have a valid \"id\" attribute");
		}

		// instance class
		String clasz = null;
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals("Class")) {
				clasz = ((Element) child).getAttribute("name");
				break;
			} else {
				child = child.getNextSibling();
			}
		}

		if (clasz == null || clasz.isEmpty()) {
			throw new NetworkParseException("An Instance element "
					+ "must have a valid \"Class\" child.");
		}

		// instance parameters
		Map<String, IExpr> parameters = parseParameters(child);

		return new Instance(path, id, clasz, parameters);
	}

	private Node parseInstances(DirectedMultigraph<Instance, Connection> graph,
			Element root) {
		Node node = root.getFirstChild();
		while (node != null) {
			if (node.getNodeName().equals("Instance")) {
				Instance instance = parseInstance(node);
				instances.put(instance.getId(), instance);
				graph.addVertex(instance);
			} else if (node.getNodeName().equals("Connection")) {
				break;
			}

			node = node.getNextSibling();
		}

		return node;
	}

	public Network parseNetwork(String path, InputStream in)
			throws ClassCastException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		this.path = path;

		// input
		DOMImplementationRegistry registry = DOMImplementationRegistry
				.newInstance();
		DOMImplementationLS impl = (DOMImplementationLS) registry
				.getDOMImplementation("Core 3.0 XML 3.0 LS");
		LSInput input = impl.createLSInput();
		input.setByteStream(in);

		// parse without comments and whitespace
		LSParser builder = impl.createLSParser(
				DOMImplementationLS.MODE_SYNCHRONOUS, null);
		DOMConfiguration config = builder.getDomConfig();
		config.setParameter("comments", false);
		config.setParameter("element-content-whitespace", false);

		// returns the document parsed from the input
		return parseXDF(builder.parse(input));
	}

	private Map<String, IExpr> parseParameters(Node node) {
		Map<String, IExpr> parameters = new HashMap<String, IExpr>();
		while (node != null) {
			if (node.getNodeName().equals("Parameter")) {
				String name = ((Element) node).getAttribute("name");
				if (name.isEmpty()) {
					throw new NetworkParseException("A Parameter element "
							+ "must have a valid \"name\" attribute");
				}

				IExpr expr = parseExpr(node.getFirstChild());
				parameters.put(name, expr);
			}

			node = node.getNextSibling();
		}

		return parameters;
	}

	private Integer parseSize(Node node) {
		while (node != null) {
			if (node.getNodeName().equals("Attribute")) {
				Element attribute = (Element) node;
				if (attribute.getAttribute("kind").equals("Value")
						&& attribute.getAttribute("name").equals("bufferSize")) {
					IExpr expr = parseExpr(attribute.getFirstChild());
					if (expr instanceof IntExpr) {
						return ((IntExpr) expr).getValue();
					} else {
						throw new NetworkParseException(
								"FIFO size: expected an integer, got: " + expr);
					}
				}
			}

			node = node.getNextSibling();
		}

		return null;
	}

	private Network parseXDF(Document doc) {
		Element root = doc.getDocumentElement();
		if (!root.getNodeName().equals("XDF")) {
			throw new NetworkParseException("Expected \"XDF\" start element");
		}

		String name = root.getAttribute("name");
		if (name.isEmpty()) {
			throw new NetworkParseException("Expected a \"name\" attribute");
		}

		instances = new HashMap<String, Instance>();
		DirectedMultigraph<Instance, Connection> graph = parseGraph(root);

		checkConnections(graph);

		return new Network(name, graph);
	}
}
