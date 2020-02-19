package play;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;

public class Function extends Scope implements FunctionType {
	protected List<Variable> parameters = new LinkedList<>();
	protected Type returnType = null;
	
	//Closure variables
	protected Set<Variable> closureVariables = null;
	
	private List<Type> paramTypes = null;
	
	protected Function(String name, Scope enclosingScope, ParserRuleContext ctx) {
		this.name = name;
		this.enclosingScope = enclosingScope;
		this.ctx = ctx;
	}
	
	@Override
	public Type getReturnType() {
		return returnType;
	}
	
	@Override
	public List<Type> getParamTypes() {
		if (paramTypes == null) {
			paramTypes = new LinkedList<Type>();
		}
		
		for (Variable var : parameters) {
			paramTypes.add(var.type);
		}
		
		return paramTypes;
	}
	
	@Override
	public String toString() {
		return "Function "+name;
	}
	
	@Override
	public boolean isType(Type type) {
		if (type instanceof FunctionType) {
			return DefaultFunctionType.isType(this, (FunctionType)type);
		}
		
		return false;
	}
	
	@Override
	public boolean matchParameterTypes(List<Type> paramTypes) {
		if (parameters.size() != paramTypes.size()) {
			return false;
		}

		for (int i = 0; i < paramTypes.size(); i++) {
			Variable var = parameters.get(i);
			Type type = paramTypes.get(i);
			if (!var.type.isType(type)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isMethod() {
		return enclosingScope instanceof Class;
	}
	
	public boolean isConstructor() {
		if (enclosingScope instanceof Class) {
			return enclosingScope.name.equals(name);
		}
		
		return false;
	}
}
