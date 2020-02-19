package play;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * annotated tree, here place all logics relating to semantic
 * analysis and AST node establishment, including:
 * 1) type information, basic types and user-defined type
 * 2) variable and function 
 * 3) scope
 *
 */
public class AnnotatedTree {
	
	//AST
	protected ParseTree ast = null;
	protected List<Type> types = new LinkedList<Type>();
	
	protected Map<ParserRuleContext, Symbol> symbolOfNode = new HashMap<>();
	
	protected Map<ParserRuleContext, Scope> node2Scope = new HashMap<>();
	
	protected Map<ParserRuleContext, Type> typeOfNode = new HashMap<>();
	
	NameSpace nameSpace = null;
	
	protected List<CompilationLog> logs = new LinkedList<>();

        protected Map<Function, Function> thisConstructorRef = new HashMap<>();

        protected Map<Function, Function> superConstructorRef = new HashMap<>();
	
	protected AnnotatedTree() {
		
	}
	
	protected void log(String message, int type, ParserRuleContext ctx) {
		CompilationLog log = new CompilationLog();
		log.ctx = ctx;
		log.line = ctx.getStart().getLine();
		log.positionInLine = ctx.getStart().getStartIndex();
		log.message = message;
		log.type = type;
		
		logs.add(log);
		
		System.out.println(log);
	}
	
	public void log(String message, ParserRuleContext ctx) {
		this.log(message, CompilationLog.ERROR, ctx);
	}
	
	protected boolean hasCompilationError() {
		for (CompilationLog log : logs) {
			if (log.type == CompilationLog.ERROR) {
				return true;
			}
		}
		
		return false;
	}
	
	protected Variable lookupVariable(Scope scope, String idName) {
		Variable rtn = scope.getVariable(idName);
		
		if (rtn == null && scope.enclosingScope != null) {
			rtn = lookupVariable(scope.enclosingScope, idName);
		}
		
		return rtn;
	}
	
	protected Class lookupClass(Scope scope, String idName) {
		Class rtn = scope.getClass(idName);
		
		if (rtn == null && scope.enclosingScope != null) {
			rtn = lookupClass(scope.enclosingScope, idName);
		}
		
		return rtn;
	}
	
	protected Type lookupType(String idName) {
		Type rtn = null;
		
		for (Type type : types) {
			if (type.getName().equals(idName)) {
				rtn = type;
				break;
			}	
		}
		
		return rtn;
	}
	
	protected Function lookupFunction(Scope scope, String idName, List<Type> paramTypes) {
		Function rtn = scope.getFunction(idName, paramTypes);
		
		if (rtn == null && scope.enclosingScope != null) {
			rtn = lookupFunction(scope.enclosingScope, idName, paramTypes);
		}
		
		return rtn;
	}
	
	protected Variable lookupFunctionVariable(Scope scope, String idName, List<Type> paramTypes) {
		Variable rtn = scope.getFunctionVariable(idName, paramTypes);
		
		if (rtn == null && scope.enclosingScope != null) {
			rtn = lookupFunctionVariable(scope.enclosingScope, idName, paramTypes);
		}
		
		return rtn;
	}
	
	protected Function lookupFunction(Scope scope, String name) {
		Function rtn = null;
		
		if (scope instanceof Class) {
			rtn = getMethodOnlyByName((Class)scope, name);
		}
		else {
			rtn = getFunctionOnlyByName(scope, name);
		}
		
		if (rtn == null && scope.enclosingScope != null) {
			rtn = lookupFunction(scope.enclosingScope, name);
		}
		
		return rtn;
	}
	
	private Function getMethodOnlyByName(Class theClass, String name) {
		Function rtn = getFunctionOnlyByName(theClass, name);
		
		if (rtn == null && theClass.getParentClass() != null) {
			rtn = getMethodOnlyByName(theClass.getParentClass(), name);
		}
		
		return rtn;
	}
	
	private Function getFunctionOnlyByName(Scope scope, String name) {
		for (Symbol s : scope.symbols) {
			if (s instanceof Function && s.name.equals(name)) {
				return (Function)s;
			}
		}
		
		return null;
	}
	
	public Scope enclosingScopeOfNode(ParserRuleContext node) {
		Scope rtn = null;
		
		ParserRuleContext parent = node.getParent();
		
		if (parent != null) {
			rtn = node2Scope.get(parent);
			if (rtn == null) {
				rtn = enclosingScopeOfNode(parent);
			}
		}
		
		return rtn;
	}
	
	
	public Function enclosingFunctionOfNode(RuleContext ctx) {
		if (ctx.parent instanceof PlayScriptParser.FunctionDeclarationContext) {
			return (Function)node2Scope.get(ctx.parent);
		}
		else if (ctx.parent == null) {
			return null;
		}
		else {
			return enclosingFunctionOfNode(ctx.parent);
		}
	}
	
	public Class enclosingClassOfNode(RuleContext ctx) {
		if (ctx.parent instanceof PlayScriptParser.ClassDeclarationContext) {
			return (Class)node2Scope.get(ctx.parent);
		}
		else if (ctx.parent == null) {
			return null;
		}
		else {
			return enclosingClassOfNode(ctx.parent);
		}
	}
	
	public String getScopeTreeString() {
		StringBuffer sb = new StringBuffer();
		scopeToString(sb, nameSpace, "");
		
		return sb.toString();
	}
	
	private void scopeToString(StringBuffer sb, Scope scope, String indent) {
		sb.append(indent).append(scope).append('\n');
		for (Symbol s : scope.symbols) {
			if (s instanceof Scope) {
				scopeToString(sb, (Scope)s, indent+"\t");
			}
			else {
				sb.append(indent).append("\t").append(s).append("\n");
			}
		}
	}
}
