package play;

import org.antlr.v4.runtime.ParserRuleContext;
import play.PlayScriptParser.*;

public class TypeChecker extends PlayScriptBaseListener{
	private AnnotatedTree at = null;
	
	public TypeChecker(AnnotatedTree at) {
		this.at = at;
	}
	
	@Override
	public void exitVariableDeclarator(VariableDeclaratorContext ctx) {
		if (ctx.variableInitializer() != null) {
			Variable variable = (Variable)at.symbolOfNode.get(ctx.variableDeclaratorId());
			Type type1 = variable.type;
			Type type2 = at.typeOfNode.get(ctx.variableInitializer());
			checkAssign(type1, type2, ctx, ctx.variableDeclaratorId(), ctx.variableInitializer());
		}
	}
	
	@Override
	public void exitExpression(ExpressionContext ctx) {
		if (ctx.bop != null && ctx.expression().size() >= 2) {
			Type type1 = at.typeOfNode.get(ctx.expression(0));
			Type type2 = at.typeOfNode.get(ctx.expression(1));
			
			switch (ctx.bop.getType()) {
			case PlayScriptParser.ADD:
				if (type1 != PrimitiveType.String && type2 != PrimitiveType.String) {
					checkNumericOperand(type1, ctx, ctx.expression(0));
					checkNumericOperand(type2, ctx, ctx.expression(1));
				}
				break;
			case PlayScriptParser.SUB:
			case PlayScriptParser.MUL:
			case PlayScriptParser.DIV:
			case PlayScriptParser.LE:
			case PlayScriptParser.LT:
			case PlayScriptParser.GE:
			case PlayScriptParser.GT:
				checkNumericOperand(type1, ctx, ctx.expression(0));
				checkNumericOperand(type2, ctx, ctx.expression(1));
				break;
			case PlayScriptParser.EQUAL:
			case PlayScriptParser.NOTEQUAL:
				break;
			
			case PlayScriptParser.AND:
			case PlayScriptParser.OR:
				checkBooleanOperand(type1, ctx, ctx.expression(0));
				checkBooleanOperand(type2, ctx, ctx.expression(1));
				break;
			
                        case PlayScriptParser.ASSIGN:
                               checkAssign(type1,type2,ctx,ctx.expression(0),ctx.expression(1));
                               break;

			case PlayScriptParser.ADD_ASSIGN:
			case PlayScriptParser.SUB_ASSIGN:
			case PlayScriptParser.MUL_ASSIGN:
			case PlayScriptParser.DIV_ASSIGN:
			case PlayScriptParser.AND_ASSIGN:
			case PlayScriptParser.OR_ASSIGN:
			case PlayScriptParser.XOR_ASSIGN:
			case PlayScriptParser.MOD_ASSIGN:
			case PlayScriptParser.LSHIFT_ASSIGN:
			case PlayScriptParser.RSHIFT_ASSIGN:
			case PlayScriptParser.URSHIFT_ASSIGN:
				if (PrimitiveType.isNumeric(type2)) {
					if (!checkNumericAssign(type2, type1)) {
						at.log("can not assign " + ctx.expression(1).getText() + " of type " + type2 + " to " + ctx.expression(0) + " of type " + type1, ctx);
					}
				}
				else {
					at.log("operand + " + ctx.expression(1).getText() + " should be numeric.", ctx);
				}
				break;
			}
		}
	}

	private void checkNumericOperand(Type type, ExpressionContext exp, ExpressionContext operand) {
		if (!(PrimitiveType.isNumeric(type))) {
			at.log("operand for arithmetic operation should be numeric: " + operand.getText(), exp);
		}
	}
	
	private void checkBooleanOperand(Type type, ExpressionContext exp, ExpressionContext operand) {
		if (!(type == PrimitiveType.Boolean)) {
			at.log("operand for boolean operation should be boolean: " + operand.getText(), exp);
		}
	}
	
	private void checkAssign(Type type1, Type type2, ParserRuleContext ctx, ParserRuleContext operand1, ParserRuleContext operand2) {
		if (PrimitiveType.isNumeric(type2)) {
			if (!checkNumericAssign(type1, type2)) {
                at.log("can not assign " + operand2.getText() + " of type " + type2 + " to " + operand1.getText() + " of type " + type1, ctx);
			}
		}
		else if (type2 instanceof Class) {
			
		}
		else if (type2 instanceof Function) {
			
		}
	}
	
	private boolean checkNumericAssign(Type from, Type to) {
		boolean canAssign = false;
		
		if (to == PrimitiveType.Double) {
			canAssign = PrimitiveType.isNumeric(from);
		}
		else if (to == PrimitiveType.Float) {
			canAssign = (from == PrimitiveType.Byte    ||
						 from == PrimitiveType.Short   ||
						 from == PrimitiveType.Integer ||
						 from == PrimitiveType.Long    ||
						 from == PrimitiveType.Float);
		}
		else if (to == PrimitiveType.Long) {
			canAssign = (from == PrimitiveType.Byte    ||
						 from == PrimitiveType.Short   ||
						 from == PrimitiveType.Integer ||
						 from == PrimitiveType.Long);
		}
		else if (to == PrimitiveType.Integer) {
			canAssign = (from == PrimitiveType.Byte    ||
						 from == PrimitiveType.Short   ||
						 from == PrimitiveType.Integer);
		}
		else if (to == PrimitiveType.Short) {
			canAssign = (from == PrimitiveType.Byte    ||
						 from == PrimitiveType.Short);
		}
		else if (to == PrimitiveType.Byte) {
			canAssign = from == PrimitiveType.Byte;
		}
		return canAssign;
	}

}
