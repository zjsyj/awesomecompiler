package play;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import play.PlayScriptParser.*;

import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class SematicValidator extends PlayScriptBaseListener {
	private AnnotatedTree at = null;
	
	public SematicValidator(AnnotatedTree at) {
		this.at = at;
	}
	
	@Override
	public void exitPrimary(PlayScriptParser.PrimaryContext ctx) {
		
	}
	
	@Override
	public void exitFunctionCall(PlayScriptParser.FunctionCallContext ctx) {
		
	}
	
	@Override
	public void exitExpression(PlayScriptParser.ExpressionContext ctx) {
		
	}
	
	@Override
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		if (at.enclosingFunctionOfNode(ctx) != null) {
			at.log("cannot declare class inside function", ctx);
		}
	}
	
	@Override
	public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
		if (ctx.typeTypeOrVoid() != null) {
			if (!hasReturnStatement(ctx)) {
				Type returnType = at.typeOfNode.get(ctx.typeTypeOrVoid());
				if (!(returnType == VoidType.instance())) {
					at.log("return statement expression in function", ctx);
				}
			}
		}
	}
	
	@Override
	public void exitVariableDeclarators(PlayScriptParser.VariableDeclaratorsContext ctx) {
		super.exitVariableDeclarators(ctx);
	}
	
	@Override
	public void exitVariableDeclarator(PlayScriptParser.VariableDeclaratorContext ctx) {
		super.exitVariableDeclarator(ctx);
	}
	
	@Override
	public void exitVariableDeclaratorId(PlayScriptParser.VariableDeclaratorIdContext ctx) {
		super.exitVariableDeclaratorId(ctx);
	}
	
	@Override
	public void exitVariableInitializer(PlayScriptParser.VariableInitializerContext ctx) {
		
	}
	
	@Override
	public void exitLiteral(PlayScriptParser.LiteralContext ctx) {
		
	}
	
	@Override
	public void exitStatement(StatementContext ctx) {
		if (ctx.RETURN() != null) {
			Function function = at.enclosingFunctionOfNode(ctx);
			if (function == null) {
				at.log("return statement not in function body", ctx);
			}
			else if (function.isConstructor() && ctx.expression() != null) {
				at.log("cannot return a value from constructor", ctx);
			}
		}
		else if (ctx.BREAK() != null) {
			if (!checkBreak(ctx)) {
				at.log("break statement not in loop or switch statements", ctx);
			}
		}
	}
	
	private boolean hasReturnStatement(ParseTree ctx) {
		boolean rtn = false;
		
		for (int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);

			if (child instanceof StatementContext && ((StatementContext) child).RETURN() != null) {
				rtn = true;
				break;
			}
			else if (!(child instanceof FunctionDeclarationContext || child instanceof ClassDeclarationContext)) {
				rtn = hasReturnStatement(child);
				if (rtn)
					break;
			}
		}
		
		return rtn;
	}
	
	private boolean checkBreak(RuleContext ctx) {
		if (ctx.parent instanceof StatementContext &&
				(((StatementContext) ctx.parent).FOR() != null ||
				((StatementContext) ctx.parent).WHILE() != null ||
		        ctx.parent instanceof SwitchBlockStatementGroupContext))  {
		        	return true;
		        }
		else if (ctx.parent == null || ctx.parent instanceof FunctionDeclarationContext) {
			return false;
		}
		else {
			return checkBreak(ctx.parent);
		}
	}
}
