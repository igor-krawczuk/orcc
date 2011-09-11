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
package net.sf.orcc.cal.validation;

import static net.sf.orcc.cal.cal.CalPackage.eINSTANCE;

import java.util.Iterator;
import java.util.List;

import net.sf.orcc.cal.cal.AstAction;
import net.sf.orcc.cal.cal.AstExpression;
import net.sf.orcc.cal.cal.AstExpressionBinary;
import net.sf.orcc.cal.cal.AstExpressionCall;
import net.sf.orcc.cal.cal.AstExpressionElsif;
import net.sf.orcc.cal.cal.AstExpressionIf;
import net.sf.orcc.cal.cal.AstExpressionIndex;
import net.sf.orcc.cal.cal.AstExpressionUnary;
import net.sf.orcc.cal.cal.AstFunction;
import net.sf.orcc.cal.cal.AstOutputPattern;
import net.sf.orcc.cal.cal.AstPort;
import net.sf.orcc.cal.cal.AstProcedure;
import net.sf.orcc.cal.cal.AstStatementAssign;
import net.sf.orcc.cal.cal.AstStatementCall;
import net.sf.orcc.cal.cal.AstStatementElsif;
import net.sf.orcc.cal.cal.AstStatementIf;
import net.sf.orcc.cal.cal.AstStatementWhile;
import net.sf.orcc.cal.cal.AstVariable;
import net.sf.orcc.cal.cal.AstVariableReference;
import net.sf.orcc.cal.cal.CalFactory;
import net.sf.orcc.cal.cal.CalPackage;
import net.sf.orcc.cal.type.Typer;
import net.sf.orcc.cal.util.Util;
import net.sf.orcc.ir.IrFactory;
import net.sf.orcc.ir.OpBinary;
import net.sf.orcc.ir.OpUnary;
import net.sf.orcc.ir.Type;
import net.sf.orcc.ir.TypeList;
import net.sf.orcc.ir.util.TypeUtil;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;

/**
 * This class describes the validation of an RVC-CAL actor. The checks tagged as
 * "expensive" are only performed when the file is saved and before code
 * generation.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TypeValidator extends AbstractCalJavaValidator {

	private void checkActionGuards(AstAction action) {
		List<AstExpression> guards = action.getGuards();
		int index = 0;
		for (AstExpression guard : guards) {
			Type type = Typer.getType(guard);
			if (!TypeUtil.isConvertibleTo(type,
					IrFactory.eINSTANCE.createTypeBool())) {
				error("Type mismatch: cannot convert from " + type + " to bool",
						action, eINSTANCE.getAstAction_Guards(), index);
			}
			index++;
		}
	}

	/**
	 * Checks the token expressions are correctly typed.
	 * 
	 * @param outputs
	 *            the output patterns of an action
	 */
	private void checkActionOutputs(List<AstOutputPattern> outputs) {
		for (AstOutputPattern pattern : outputs) {
			AstPort port = pattern.getPort();

			Type portType = Typer.getType(port);
			AstExpression astRepeat = pattern.getRepeat();
			if (astRepeat == null) {
				List<AstExpression> values = pattern.getValues();
				int index = 0;
				for (AstExpression value : values) {
					Type type = Typer.getType(value);
					if (!TypeUtil.isConvertibleTo(type, portType)) {
						error("this expression must be of type " + portType,
								pattern,
								eINSTANCE.getAstOutputPattern_Values(), index);
					}
					index++;
				}
			} else {
				int repeat = Util.getIntValue(astRepeat);
				if (repeat != 1) {
					// each value is supposed to be a list
					List<AstExpression> values = pattern.getValues();
					int index = 0;
					for (AstExpression value : values) {
						Type type = Typer.getType(value);
						if (type.isList()) {
							TypeList typeList = (TypeList) type;
							Type lub = TypeUtil.getLub(portType,
									typeList.getType());
							if (lub != null && typeList.getSize() >= repeat) {
								continue;
							}
						}

						error("Type mismatch: expected " + portType + "["
								+ repeat + "]", pattern,
								eINSTANCE.getAstOutputPattern_Values(), index);
						index++;
					}
				}
			}
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstAction(AstAction action) {
		checkActionGuards(action);
		checkActionOutputs(action.getOutputs());
	}

	@Check(CheckType.NORMAL)
	public void checkAstExpressionBinary(AstExpressionBinary expression) {
		OpBinary op = OpBinary.getOperator(expression.getOperator());
		checkTypeBinary(op, Typer.getType(expression.getLeft()),
				Typer.getType(expression.getRight()), expression,
				eINSTANCE.getAstExpressionBinary_Operator(), -1);
	}

	@Check(CheckType.NORMAL)
	public void checkAstExpressionCall(AstExpressionCall astCall) {
		AstFunction function = astCall.getFunction();

		String name = function.getName();
		List<AstExpression> parameters = astCall.getParameters();
		if (function.getParameters().size() != parameters.size()) {
			error("function " + name + " takes "
					+ function.getParameters().size() + " arguments.", astCall,
					eINSTANCE.getAstExpressionCall_Function(), -1);
			return;
		}

		Iterator<AstVariable> itFormal = function.getParameters().iterator();
		Iterator<AstExpression> itActual = parameters.iterator();
		int index = 0;
		while (itFormal.hasNext() && itActual.hasNext()) {
			Type formalType = Typer.getType(itFormal.next());
			AstExpression expression = itActual.next();
			Type actualType = Typer.getType(expression);

			// check types
			if (!TypeUtil.isConvertibleTo(actualType, formalType)) {
				error("Type mismatch: cannot convert from " + actualType
						+ " to " + formalType, astCall,
						eINSTANCE.getAstExpressionCall_Parameters(), index);
			}
			index++;
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstExpressionElsif(AstExpressionElsif expression) {
		Type type = Typer.getType(expression.getCondition());
		if (type == null || !type.isBool()) {
			error("Cannot convert " + type + " to bool", expression,
					eINSTANCE.getAstExpressionElsif_Condition(), -1);
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstExpressionIf(AstExpressionIf expression) {
		Type type = Typer.getType(expression.getCondition());
		if (type == null || !type.isBool()) {
			error("Cannot convert " + type + " to bool", expression,
					eINSTANCE.getAstExpressionIf_Condition(), -1);
		}

		Type typeThen = Typer.getType(expression.getThen());
		type = typeThen;
		int index = 0;
		for (AstExpressionElsif elsif : expression.getElsifs()) {
			Type typeElsif = Typer.getType(elsif);
			type = TypeUtil.getLub(type, typeElsif);
			if (type == null) {
				error("Type mismatch: cannot convert " + typeElsif + " to "
						+ typeThen, expression,
						eINSTANCE.getAstExpressionIf_Elsifs(), index);
			}
			index++;
		}

		Type typeElse = Typer.getType(expression.getElse());
		type = TypeUtil.getLub(type, typeElse);
		if (type == null) {
			error("Type mismatch: cannot convert " + typeElse + " to " + type,
					expression, eINSTANCE.getAstExpressionIf_Else(), -1);
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstExpressionIndex(AstExpressionIndex expression) {
		AstVariable variable = expression.getSource().getVariable();
		Type type = Typer.getType(variable);

		List<AstExpression> indexes = expression.getIndexes();
		int errorIdx = 0;
		for (AstExpression index : indexes) {
			Type subType = Typer.getType(index);
			if (type.isList()) {
				if (subType != null && (subType.isInt() || subType.isUint())) {
					type = ((TypeList) type).getType();
				} else {
					error("index must be an integer", index,
							eINSTANCE.getAstExpressionIndex_Indexes(), errorIdx);
				}
			} else {
				error("Cannot convert " + type + " to List", expression,
						eINSTANCE.getAstExpressionIndex_Source(), -1);
			}
			errorIdx++;
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstExpressionUnary(AstExpressionUnary expression) {
		OpUnary op = OpUnary.getOperator(expression.getUnaryOperator());
		Type type = Typer.getType(expression.getExpression());
		if (type == null) {
			return;
		}

		switch (op) {
		case BITNOT:
			if (!(type.isInt() || type.isUint())) {
				error("Cannot convert " + type + " to int/uint", expression,
						eINSTANCE.getAstExpressionUnary_Expression(), -1);
			}
			break;
		case LOGIC_NOT:
			if (!type.isBool()) {
				error("Cannot convert " + type + " to boolean", expression,
						eINSTANCE.getAstExpressionUnary_Expression(), -1);
			}
			break;
		case MINUS:
			if (!type.isUint() && !type.isInt()) {
				error("Cannot convert " + type + " to int", expression,
						eINSTANCE.getAstExpressionUnary_Expression(), -1);
			}
			break;
		case NUM_ELTS:
			if (!type.isList()) {
				error("Cannot convert " + type + " to List", expression,
						eINSTANCE.getAstExpressionUnary_Expression(), -1);
			}
			break;
		default:
			error("Unknown unary operator", expression,
					eINSTANCE.getAstExpressionUnary_Expression(), -1);
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstFunction(final AstFunction function) {
		if (!function.isNative()) {
			checkReturnType(function);
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstStatementAssign(AstStatementAssign assign) {
		AstVariable variable = assign.getTarget().getVariable();
		if (variable.isConstant()
				|| variable.eContainingFeature() == CalPackage.Literals.AST_ACTOR__PARAMETERS) {
			error("The variable " + variable.getName() + " is not assignable",
					eINSTANCE.getAstStatementAssign_Target());
		}

		// create expression
		AstExpressionIndex expression = CalFactory.eINSTANCE
				.createAstExpressionIndex();

		// set reference
		AstVariableReference reference = CalFactory.eINSTANCE
				.createAstVariableReference();
		reference.setVariable(variable);
		expression.setSource(reference);

		// copy indexes
		expression.getIndexes().addAll(EcoreUtil.copyAll(assign.getIndexes()));

		// check types
		Type targetType = Typer.getType(expression);
		Type type = Typer.getType(assign.getValue());
		if (!TypeUtil.isConvertibleTo(type, targetType)) {
			error("Type mismatch: cannot convert from " + type + " to "
					+ targetType, assign,
					eINSTANCE.getAstStatementAssign_Value(), -1);
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstStatementCall(AstStatementCall astCall) {
		AstProcedure procedure = astCall.getProcedure();
		String name = procedure.getName();
		List<AstExpression> parameters = astCall.getParameters();

		if (procedure.eContainer() == null) {
			if ("print".equals(name) || "println".equals(name)) {
				if (parameters.size() > 1) {
					error("built-in procedure " + name
							+ " takes at most one expression", astCall,
							eINSTANCE.getAstStatementCall_Procedure(), -1);
				}
			}

			return;
		}

		if (procedure.getParameters().size() != parameters.size()) {
			error("procedure " + name + " takes "
					+ procedure.getParameters().size() + " arguments.",
					astCall, eINSTANCE.getAstStatementCall_Procedure(), -1);
			return;
		}

		Iterator<AstVariable> itFormal = procedure.getParameters().iterator();
		Iterator<AstExpression> itActual = parameters.iterator();
		int index = 0;
		while (itFormal.hasNext() && itActual.hasNext()) {
			Type formalType = Typer.getType(itFormal.next());
			AstExpression expression = itActual.next();
			Type actualType = Typer.getType(expression);

			// check types
			if (!TypeUtil.isConvertibleTo(actualType, formalType)) {
				error("Type mismatch: cannot convert from " + actualType
						+ " to " + formalType, astCall,
						eINSTANCE.getAstStatementCall_Parameters(), index);
			}
			index++;
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstStatementElsif(AstStatementElsif elsIf) {
		Type type = Typer.getType(elsIf.getCondition());
		if (!TypeUtil.isConvertibleTo(type,
				IrFactory.eINSTANCE.createTypeBool())) {
			error("Type mismatch: cannot convert from " + type + " to bool",
					elsIf, eINSTANCE.getAstStatementElsif_Condition(), -1);
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstStatementIf(AstStatementIf astIf) {
		Type type = Typer.getType(astIf.getCondition());
		if (!TypeUtil.isConvertibleTo(type,
				IrFactory.eINSTANCE.createTypeBool())) {
			error("Type mismatch: cannot convert from " + type + " to bool",
					astIf, eINSTANCE.getAstStatementIf_Condition(), -1);
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstStatementWhile(AstStatementWhile astWhile) {
		Type type = Typer.getType(astWhile.getCondition());
		if (!TypeUtil.isConvertibleTo(type,
				IrFactory.eINSTANCE.createTypeBool())) {
			error("Type mismatch: cannot convert from " + type + " to bool",
					astWhile, eINSTANCE.getAstStatementWhile_Condition(), -1);
		}
	}

	@Check(CheckType.NORMAL)
	public void checkAstVariable(AstVariable variable) {
		AstExpression value = variable.getValue();
		if (value != null) {
			// check types
			Type targetType = Typer.getType(variable);
			Type type = Typer.getType(value);
			if (!TypeUtil.isConvertibleTo(type, targetType)) {
				error("Type mismatch: cannot convert from " + type + " to "
						+ targetType, variable,
						eINSTANCE.getAstVariable_Value(), -1);
			}
		}
	}

	private void checkReturnType(AstFunction function) {
		Type returnType = Typer.getType(function);
		Type expressionType = Typer.getType(function.getExpression());
		if (!TypeUtil.isConvertibleTo(expressionType, returnType)) {
			error("Type mismatch: cannot convert from " + expressionType
					+ " to " + returnType, function,
					eINSTANCE.getAstFunction_Expression(), -1);
		}
	}

	/**
	 * Check that the type of a binary expression whose left operand has type t1
	 * and right operand has type t2, and whose operator is given, is correct.
	 * 
	 * @param op
	 *            operator
	 * @param t1
	 *            type of the first operand
	 * @param t2
	 *            type of the second operand
	 * @param source
	 *            source object
	 * @param feature
	 *            feature
	 */
	private void checkTypeBinary(OpBinary op, Type t1, Type t2, EObject source,
			EStructuralFeature feature, int index) {
		switch (op) {
		case BITAND:
			if (!t1.isInt() && !t1.isUint()) {
				error("Cannot convert " + t1 + " to int/uint", source, feature,
						index);
			}
			if (!t2.isInt() && !t2.isUint()) {
				error("Cannot convert " + t2 + " to int/uint", source, feature,
						index);
			}
			break;

		case BITOR:
		case BITXOR:
			if (!t1.isInt() && !t1.isUint()) {
				error("Cannot convert " + t1 + " to int/uint", source, feature,
						index);
			}
			if (!t2.isInt() && !t2.isUint()) {
				error("Cannot convert " + t2 + " to int/uint", source, feature,
						index);
			}
			break;

		case TIMES:
			if (!t1.isInt() && !t1.isUint()) {
				error("Cannot convert " + t1 + " to int/uint", source, feature,
						index);
			}
			if (!t2.isInt() && !t2.isUint()) {
				error("Cannot convert " + t2 + " to int/uint", source, feature,
						index);
			}
			break;

		case MINUS:
			if (!t1.isInt() && !t1.isUint()) {
				error("Cannot convert " + t1 + " to int/uint", source, feature,
						index);
			}
			if (!t2.isInt() && !t2.isUint()) {
				error("Cannot convert " + t2 + " to int/uint", source, feature,
						index);
			}
			break;

		case PLUS:
			if (t1.isString() && t2.isList()) {
				error("Cannot convert " + t2 + " to String", source, feature,
						index);
			}
			if (t2.isString() && t1.isList()) {
				error("Cannot convert " + t1 + " to String", source, feature,
						index);
			}
			if (t1.isBool() && !t2.isString() || !t1.isString() && t2.isBool()) {
				error("Addition is not defined for booleans", source, feature,
						index);
			}
			break;

		case DIV:
		case DIV_INT:
		case SHIFT_RIGHT:
			if (!t1.isInt() && !t1.isUint()) {
				error("Cannot convert " + t1 + " to int/uint", source, feature,
						index);
			}
			if (!t2.isInt() && !t2.isUint()) {
				error("Cannot convert " + t2 + " to int/uint", source, feature,
						index);
			}
			break;

		case MOD:
			if (!t1.isInt() && !t1.isUint()) {
				error("Cannot convert " + t1 + " to int/uint", source, feature,
						index);
			}
			if (!t2.isInt() && !t2.isUint()) {
				error("Cannot convert " + t2 + " to int/uint", source, feature,
						index);
			}
			break;

		case SHIFT_LEFT:
			if (!t1.isInt() && !t1.isUint()) {
				error("Cannot convert " + t1 + " to int/uint", source, feature,
						index);
			}
			if (!t2.isInt() && !t2.isUint()) {
				error("Cannot convert " + t2 + " to int/uint", source, feature,
						index);
			}
			break;

		case EQ:
		case GE:
		case GT:
		case LE:
		case LT:
		case NE:
			Type type = TypeUtil.getLub(t1, t2);
			if (type == null) {
				error("Incompatible operand types " + t1 + " and " + t2,
						source, feature, index);
			}
			break;

		case EXP:
			error("Operator ** not implemented", source, feature, index);
			break;

		case LOGIC_AND:
		case LOGIC_OR:
			if (!t1.isBool()) {
				error("Cannot convert " + t1 + " to bool", source, feature,
						index);
			}
			if (!t2.isBool()) {
				error("Cannot convert " + t2 + " to bool", source, feature,
						index);
			}
			break;
		}
	}

}
