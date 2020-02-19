package play;

import play.PlayScriptParser.*;

public class TypeResolver extends PlayScriptBaseListener {
	private AnnotatedTree at = null;
	
	public TypeResolver(AnnotatedTree at) {
		this.at = at;
	}
	
	@Override
	public void enterVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
		String idName = ctx.IDENTIFIER().getText();
		Scope  scope = at.enclosingScopeOfNode(ctx);
		Variable variable = new Variable(idName, scope, ctx);
		
		if (Scope.getVariable(scope, idName) != null) {
			at.log("Variable or parameter already declared: " + idName, ctx);
		}
		
		scope.addSymbol(variable);
		at.symbolOfNode.put(ctx, variable);
	}
	
	@Override
	public void exitVariableDeclarators(VariableDeclaratorsContext ctx) {
		Type type = (Type)at.typeOfNode.get(ctx.typeType());
		
		for (VariableDeclaratorContext child : ctx.variableDeclarator()) {
			Variable variable = (Variable)at.symbolOfNode.get(child.variableDeclaratorId());
			variable.type = type;
		}
	}
	
	@Override
	public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
		Function function = (Function)at.node2Scope.get(ctx);
		if (ctx.typeTypeOrVoid() != null) {
			function.returnType = at.typeOfNode.get(ctx.typeTypeOrVoid());
		}
		else {
			
		}
		
		Scope scope = at.enclosingScopeOfNode(ctx);
		Function found = Scope.getFunction(scope, function.name, function.getParamTypes());
		if (found != null && found != function) {
			at.log("Function or method already declared: " + ctx.getText(), ctx);
		}
	}
	
	@Override
	public void exitFormalParameter(FormalParameterContext ctx) {
		Type type = at.typeOfNode.get(ctx.typeType());
		Variable variable = (Variable)at.symbolOfNode.get(ctx.variableDeclaratorId());
		variable.type = type;
		
		Scope scope = at.enclosingScopeOfNode(ctx);
		if (scope instanceof Function) {
			((Function) scope).parameters.add(variable);
		}
	}
	
	@Override
	public void enterClassDeclaration(ClassDeclarationContext ctx) {
		Class theClass = (Class)at.node2Scope.get(ctx);
		
		if (ctx.EXTENDS() != null) {
			String parentClassName = ctx.typeType().getText();
			Type type = at.lookupType(parentClassName);
			if (type != null && type instanceof Class) {
				theClass.setParentClass((Class)type);
			}
			else {
				at.log("unknown class: " + parentClassName, ctx);
			}
		}
	}
	
	@Override
	public void exitTypeTypeOrVoid(TypeTypeOrVoidContext ctx) {
		if (ctx.VOID() != null) {
			at.typeOfNode.put(ctx, VoidType.instance());
		}
		else if (ctx.typeType() != null) {
			at.typeOfNode.put(ctx, (Type)at.typeOfNode.get(ctx.typeType()));
		}
	}
	
	@Override
	public void exitTypeType(TypeTypeContext ctx) {
		if (ctx.classOrInterfaceType() != null) {
			Type type = (Type)at.typeOfNode.get(ctx.classOrInterfaceType());
			at.typeOfNode.put(ctx, type);
		}
		else if (ctx.functionType() != null) {
			Type type = (Type)at.typeOfNode.get(ctx.functionType());
			at.typeOfNode.put(ctx, type);
		}
		else if (ctx.primitiveType() != null) {
			Type type = (Type)at.typeOfNode.get(ctx.primitiveType());
			at.typeOfNode.put(ctx, type);
		}
	}
	
	@Override
	public void enterClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
		if (ctx.IDENTIFIER() != null) {
			Scope scope = at.enclosingScopeOfNode(ctx);
			String idName = ctx.getText();
			Class theClass = at.lookupClass(scope, idName);
			at.typeOfNode.put(ctx, theClass);
		}
	}
	
	@Override
	public void exitFunctionType(FunctionTypeContext ctx) {
		DefaultFunctionType functionType = new DefaultFunctionType();
		
		at.types.add(functionType);
		at.typeOfNode.put(ctx, functionType);
		
		functionType.returnType = (Type)at.typeOfNode.get(ctx.typeTypeOrVoid());
		if (ctx.typeList() != null) {
			TypeListContext tcl = (TypeListContext)ctx.typeList();
			for (TypeTypeContext ttc : tcl.typeType()) {
				Type type = (Type)at.typeOfNode.get(ttc);
				functionType.paramTypes.add(type);
			}
		}
	}
	
	@Override
	public void exitPrimitiveType(PrimitiveTypeContext ctx) {
		Type type = null;
		if (ctx.BOOLEAN() != null) {
			type = PrimitiveType.Boolean;
		}
		else if (ctx.BYTE() != null) {
			type = PrimitiveType.Byte;
		}
		else if (ctx.SHORT() != null) {
			type = PrimitiveType.Short;
		}
		else if (ctx.INT() != null) {
			type = PrimitiveType.Integer;
		}
		else if (ctx.LONG() != null) {
			type = PrimitiveType.Long;
		}
		else if (ctx.FLOAT() != null) {
			type = PrimitiveType.Float;
		}
		else if (ctx.DOUBLE() != null) {
			type = PrimitiveType.Double;
		}
		else if (ctx.CHAR() != null) {
			type = PrimitiveType.Char;
		}
		else if (ctx.STRING() != null) {
			type = PrimitiveType.String;
		}
		at.typeOfNode.put(ctx, type);
	}
}
