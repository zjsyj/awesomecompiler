package play;

import org.antlr.v4.runtime.ParserRuleContext;
import play.PlayScriptParser.*;

import java.util.Stack;

public class TypeAndScopeScanner extends PlayScriptBaseListener {
	private AnnotatedTree at = null;
	
	private Stack<Scope> scopeStack = new Stack<>();
	
	public TypeAndScopeScanner(AnnotatedTree at) {
		this.at = at;
	}
	
	private Scope pushScope(Scope scope, ParserRuleContext ctx) {
		at.node2Scope.put(ctx, scope);
		
		scope.ctx = ctx;
		scopeStack.push(scope);
		
		return scope;
	}
	
	private void popScope() {
		scopeStack.pop();
	}
	
	private Scope currentScope() {
		if (scopeStack.size() > 0) {
			return scopeStack.peek();
		}
		else {
			return null;
		}
	}
	
	@Override
	public void enterProg(ProgContext ctx) {
		NameSpace scope = new NameSpace("", currentScope(), ctx);
		at.nameSpace = scope;
		pushScope(scope, ctx);
	}
	
	@Override
	public void exitProg(ProgContext ctx) {
		popScope();
	}
	
	@Override
	public void enterBlock(BlockContext ctx) {
		if (!(ctx.parent instanceof FunctionBodyContext)) {
			BlockScope scope = new BlockScope(currentScope(), ctx);
			currentScope().addSymbol(scope);
			pushScope(scope, ctx);
		}
	}
	
	@Override
	public void exitBlock(BlockContext ctx) {
		if (!(ctx.parent instanceof FunctionBodyContext)) {
			popScope();
		}
	}
	
	@Override
	public void enterStatement(StatementContext ctx) {
		if (ctx.FOR() != null) {
			BlockScope scope = new BlockScope(currentScope(), ctx);
			currentScope().addSymbol(scope);
			pushScope(scope, ctx);
		}
	}
	
	@Override
	public void exitStatement(StatementContext ctx) {
		if (ctx.FOR() != null) {
			popScope();
		}
	}
	
	@Override
	public void enterFunctionDeclaration(FunctionDeclarationContext ctx) {
		String idName = ctx.IDENTIFIER().getText();
		
		Function function = new Function(idName, currentScope(), ctx);
		
		at.types.add(function);
		
		currentScope().addSymbol(function);
		
		pushScope(function, ctx);
	}
	
	@Override
	public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
		popScope();
	}
	
	@Override
	public void enterClassDeclaration(ClassDeclarationContext ctx) {
		String idName = ctx.IDENTIFIER().getText();
		
		Class theClass = new Class(idName, ctx);
		
		at.types.add(theClass);
		
		if (at.lookupClass(currentScope(), idName) != null) {
			at.log("duplicate class name: " + idName, ctx);
		}
		
		currentScope().addSymbol(theClass);
		
		pushScope(theClass, ctx);
	}
	
	@Override
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		popScope();
	}
}
