package play;

import play.PlayScriptParser.*;

import java.util.LinkedList;
import java.util.List;

public class RefResolver extends PlayScriptBaseListener {
	private AnnotatedTree at = null;
	
	private List<FunctionCallContext> thisConstructorList = new LinkedList<>();
	private List<FunctionCallContext> superConstructorList = new LinkedList<>();
	
	public RefResolver(AnnotatedTree at) {
		this.at = at;
	}
	
	@Override
	public void exitPrimary(PrimaryContext ctx) {
		Scope scope = at.enclosingScopeOfNode(ctx);
		Type type = null;
		
		if (ctx.IDENTIFIER() != null) {
			String idName = ctx.IDENTIFIER().getText();
			Variable variable = at.lookupVariable(scope, idName);

			if (variable == null) {
				Function function = at.lookupFunction(scope, idName);
				if (function != null) {
					at.symbolOfNode.put(ctx, function);
					type = function;
				}
				else {
					at.log("unknown variable or function: " + idName, ctx);
				}
			}
			else {
				at.symbolOfNode.put(ctx, variable);
				type = variable.type;
			}
		}
		else if (ctx.literal() != null) {
			type = at.typeOfNode.get(ctx.literal());
		}
		else if (ctx.expression() != null) {
			type = at.typeOfNode.get(ctx.expression());
		}
		else if (ctx.THIS() != null) {
			Class theClass = at.enclosingClassOfNode(ctx);
			if (theClass != null) {
				This variable = theClass.getThis();
				at.symbolOfNode.put(ctx, variable);
				type = theClass;
			}
			else {
				at.log("keyword \"this\" can only be used inside a class", ctx);
			}
		}
		else if (ctx.SUPER() != null) {
			Class theClass = at.enclosingClassOfNode(ctx);
			if (theClass != null) {
				Super variable = theClass.getSuper();
				at.symbolOfNode.put(ctx, variable);
				type = theClass;
			}
			else {
				at.log("keyword \"super\" can only be used inside a class", ctx);
			}
		}
		
		at.typeOfNode.put(ctx, type);
	}
	
	@Override
	public void exitFunctionCall(FunctionCallContext ctx) {
		if (ctx.THIS() != null) {
			thisConstructorList.add(ctx);
			return;
		}
		else if (ctx.SUPER() != null) {
			superConstructorList.add(ctx);
			return;
		}
		
		if (ctx.IDENTIFIER().getText().equals("println")) {
			return;
		}
		
		String idName = ctx.IDENTIFIER().getText();
		List<Type> paramTypes = getParamTypes(ctx);
		boolean found = false;
		
		if (ctx.parent instanceof ExpressionContext) {
			ExpressionContext exp = (ExpressionContext)ctx.parent;
			if (exp.bop != null && exp.bop.getType() == PlayScriptParser.DOT) {
				Symbol symbol = at.symbolOfNode.get(exp.expression(0));
				if (symbol instanceof Variable && ((Variable) symbol).type instanceof Class) {
					Class theClass = (Class)((Variable) symbol).type;
					Function function = theClass.getFunction(idName, paramTypes);
					
					if (function != null) {
						found = true;
						at.symbolOfNode.put(ctx, function);
						at.typeOfNode.put(ctx, function.getReturnType());
					}
					else {
						Variable funcVar = theClass.getFunctionVariable(idName, paramTypes);
						if (funcVar != null) {
							found = true;
							at.symbolOfNode.put(ctx, funcVar);
							at.typeOfNode.put(ctx, ((FunctionType) funcVar.type).getReturnType());
						}
						else {
							at.log("unable to find method " + idName + " in Class " + theClass.name, exp);
						}
					}
				}
				else {
					at.log("unable to resolve a class", ctx);
				}
			}
		}
		
		Scope scope = at.enclosingScopeOfNode(ctx);
		
		if (!found) {
			Function function = at.lookupFunction(scope, idName, paramTypes);
			if (function != null) {
				found = true;
				at.symbolOfNode.put(ctx, function);
				at.typeOfNode.put(ctx, function.returnType);
			}
		}
		
		if (!found) {
			Class theClass = at.lookupClass(scope, idName);
			if (theClass != null) {
				Function function = theClass.findConstructor(paramTypes);
				if (function != null) {
					found = true;
					at.symbolOfNode.put(ctx, function);
				}
				else if (ctx.expressionList() == null) {
					found = true;
					at.symbolOfNode.put(ctx, theClass.defaultConstructor());
				}
				else {
					at.log("unknown class constructor: " + ctx.getText(), ctx);
				}
				
				at.typeOfNode.put(ctx, theClass);
			}
			else {
				Variable variable = at.lookupFunctionVariable(scope, idName, paramTypes);
				if (variable != null && variable.type instanceof FunctionType) {
					found = true;
					at.symbolOfNode.put(ctx, variable);
					at.typeOfNode.put(ctx, variable.type);
				}
				else {
					at.log("unknown function or function variable: " + ctx.getText(), ctx);
				}
			}
		}
	}
	
	private void resolveThisConstructorCall(FunctionCallContext ctx) {
		Class theClass = at.enclosingClassOfNode(ctx);
		if (theClass != null) {
			Function function = at.enclosingFunctionOfNode(ctx);
			if (function != null && function.isConstructor()) {
                FunctionDeclarationContext fdx = (FunctionDeclarationContext) function.ctx;
                if (!firstStatmentInFunction(fdx, ctx)){
                    at.log("this() must be first statement in a constructor", ctx);
                    return;
                }

				List<Type> paramTypes = getParamTypes(ctx);
				Function refered = theClass.findConstructor(paramTypes);
				if (refered != null) {
					at.symbolOfNode.put(ctx, refered);
					at.typeOfNode.put(ctx, theClass);
                                        at.thisConstructorRef.put(function, refered);
				}
				else if (paramTypes.size() == 0) {
					at.symbolOfNode.put(ctx, theClass.defaultConstructor());
					at.typeOfNode.put(ctx, theClass);
                                        at.thisConstructorRef.put(function, theClass.defaultConstructor());
				}
				else {
					at.log("can not find a constructor matches this()", ctx);
				}
			}
			else {
				at.log("this() should only be called inside a class constructor", ctx);
			}
		}
		else {
			at.log("this() should only be called inside a class", ctx);
		}
	}

    private boolean firstStatmentInFunction(FunctionDeclarationContext fdx, FunctionCallContext ctx){
        if (fdx.functionBody().block().blockStatements().blockStatement(0).statement()!= null
            && fdx.functionBody().block().blockStatements().blockStatement(0).statement().expression()!= null
            && fdx.functionBody().block().blockStatements().blockStatement(0).statement().expression().functionCall()==ctx){
            return true;
        }

        return false;
    }
	
	private void resolveSuperConstructorCall(FunctionCallContext ctx) {
		Class theClass = at.enclosingClassOfNode(ctx);
		
		if (theClass != null) {
			Function function = at.enclosingFunctionOfNode(ctx);
			if (function != null && function.isConstructor()) {
				Class parentClass = theClass.getParentClass();
				if (parentClass != null) {
                                          FunctionDeclarationContext fdx = (FunctionDeclarationContext) function.ctx;
                                          if (!firstStatmentInFunction(fdx, ctx)){
                                               at.log("super() must be first statement in a constructor", ctx);
                                               return;
                                        }

					List<Type> paramTypes = getParamTypes(ctx);
					Function refered = parentClass.findConstructor(paramTypes);
					if (refered != null) {
						at.symbolOfNode.put(ctx, refered);
						at.typeOfNode.put(ctx, theClass);
                                                at.superConstructorRef.put(function, refered);

					}
					else if (paramTypes.size() == 0) {
						at.symbolOfNode.put(ctx, parentClass.defaultConstructor());
						at.typeOfNode.put(ctx, theClass);
                                                at.superConstructorRef.put(function, theClass.defaultConstructor());
					}
					else {
						at.log("can not find a constructor matches super()", ctx);
					}
				}
			}
			else {
				
			}
		}
		else {
			at.log("super() should be only called inside a clss", ctx);
		}
	}
	
	private List<Type> getParamTypes(FunctionCallContext ctx) {
		List<Type> paramTypes = new LinkedList<>();
		
		if (ctx.expressionList() != null) {
			for (ExpressionContext exp : ctx.expressionList().expression()) {
				Type type = at.typeOfNode.get(exp);
				paramTypes.add(type);
			}
		}

		return paramTypes;
	}
	
	@Override
	public void exitExpression(ExpressionContext ctx) {
		Type type = null;
		
		if (ctx.bop != null && ctx.bop.getType() == PlayScriptParser.DOT) {
			Symbol symbol = at.symbolOfNode.get(ctx.expression(0));
			if (symbol instanceof Variable && ((Variable) symbol).type instanceof Class ) {
				Class theClass = (Class)((Variable) symbol).type;
				
				if (ctx.IDENTIFIER() != null) {
					String idName = ctx.IDENTIFIER().getText();
					Variable variable = at.lookupVariable(theClass, idName);
					if (variable != null) {
						at.symbolOfNode.put(ctx, variable);
						type = variable.type;
					}
					else {
						at.log("unable to find field " + idName + "in class " + theClass.name, ctx);
					}
				}
				else if (ctx.functionCall() != null) {
					type = at.typeOfNode.get(ctx.functionCall());
				}
			}
			else {
				at.log("symbol is not a qualified object: " + symbol, ctx);
			}
		}
		else if (ctx.primary() != null) {
			Symbol symbol = at.symbolOfNode.get(ctx.primary());
			at.symbolOfNode.put(ctx, symbol);
		}
		
		if (ctx.primary() != null) {
			type = at.typeOfNode.get(ctx.primary());
		}
		else if (ctx.functionCall() != null) {
			type = at.typeOfNode.get(ctx.functionCall());
		}
		else if (ctx.bop != null && ctx.expression().size() >= 2) {
			Type type1 = at.typeOfNode.get(ctx.expression(0));
			Type type2 = at.typeOfNode.get(ctx.expression(1));
			
			switch (ctx.bop.getType()) {
				case PlayScriptParser.ADD:
					if (type1 == PrimitiveType.String || type2 == PrimitiveType.String) {
						type = PrimitiveType.String;
					}
					else if (type1 instanceof PrimitiveType && type2 instanceof PrimitiveType) {
						type = PrimitiveType.getUpperType(type1, type2);
					}
					else {
						at.log("operand should be PrimitiveType for additive and multiplicative operation", ctx);
					}
					break;
				case PlayScriptParser.MUL:
				case PlayScriptParser.SUB:
				case PlayScriptParser.DIV:
					if (type1 instanceof PrimitiveType && type2 instanceof PrimitiveType) {
						type = PrimitiveType.getUpperType(type1, type2);
					}
					else {
						at.log("operand should be PrimitiveType for additive and multiplicative operation", ctx);
					}
					
					break;
				case PlayScriptParser.EQUAL:
				case PlayScriptParser.NOTEQUAL:
				case PlayScriptParser.LE:
				case PlayScriptParser.LT:
				case PlayScriptParser.GE:
				case PlayScriptParser.GT:
				case PlayScriptParser.AND:
				case PlayScriptParser.OR:
				case PlayScriptParser.BANG:
					type = PrimitiveType.Boolean;
					break;
				case PlayScriptParser.ASSIGN:
				case PlayScriptParser.ADD_ASSIGN:
				case PlayScriptParser.SUB_ASSIGN:
				case PlayScriptParser.MUL_ASSIGN:
				case PlayScriptParser.DIV_ASSIGN:
				case PlayScriptParser.OR_ASSIGN:
				case PlayScriptParser.AND_ASSIGN:
				case PlayScriptParser.XOR_ASSIGN:
				case PlayScriptParser.MOD_ASSIGN:
				case PlayScriptParser.LSHIFT_ASSIGN:
				case PlayScriptParser.RSHIFT_ASSIGN:
				case PlayScriptParser.URSHIFT_ASSIGN:
					type = type1;
					break;
			}
		}
		
		at.typeOfNode.put(ctx, type);
	}
	
	@Override
	public void exitVariableInitializer(VariableInitializerContext ctx) {
		if (ctx.expression() != null) {
			at.typeOfNode.put(ctx, at.typeOfNode.get(ctx.expression()));
		}
	}
	
	@Override
	public void exitLiteral(LiteralContext ctx) {
		if (ctx.BOOL_LITERAL() != null) {
			at.typeOfNode.put(ctx, PrimitiveType.Boolean);
		}
		else if (ctx.CHAR_LITERAL() != null) {
			at.typeOfNode.put(ctx, PrimitiveType.Char);
		}
		else if (ctx.NULL_LITERAL() != null) {
			at.typeOfNode.put(ctx, PrimitiveType.Null);
		}
		else if (ctx.STRING_LITERAL() != null) {
			at.typeOfNode.put(ctx, PrimitiveType.String);
		}
		else if (ctx.integerLiteral() != null) {
			at.typeOfNode.put(ctx, PrimitiveType.Integer);
		}
		else if (ctx.floatLiteral() != null) {
			at.typeOfNode.put(ctx, PrimitiveType.Float);
		}
		
	}
	
	@Override
	public void exitProg(ProgContext ctx) {
		for (FunctionCallContext fcc : thisConstructorList) {
			resolveThisConstructorCall(fcc);
		}
		
		for (FunctionCallContext fcc : superConstructorList) {
			resolveSuperConstructorCall(fcc);
		}
	}
}
