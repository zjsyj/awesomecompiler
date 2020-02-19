package play;

import java.util.LinkedList;
import java.util.List;

public abstract class Scope extends Symbol {
	protected List<Symbol> symbols = new LinkedList<>();
	
	protected void addSymbol(Symbol symbol) {
		symbols.add(symbol);
		symbol.enclosingScope = this;
	}
	
	protected Variable getVariable(String name) {
		return getVariable(this, name);
	}
	
	protected static Variable getVariable(Scope scope, String name) {
		for (Symbol s : scope.symbols) {
			if (s instanceof Variable && s.name.equals(name)) {
				return (Variable)s;
			}
		}
		
		return null;
	}

	protected Function getFunction(String name, List<Type> paramTypes) {
		return getFunction(this, name, paramTypes);
	}
	
	protected static Function getFunction(Scope scope, String name, List<Type> paramTypes) {
		Function rtn = null;
		
		for (Symbol s : scope.symbols) {
			if (s instanceof Function && s.name.equals(name)) {
				Function function = (Function)s;
				if (function.matchParameterTypes(paramTypes)) {
					rtn = function;
					break;
				}
			}
		}
		
		return rtn;
	}
	
	protected Variable getFunctionVariable(String name, List<Type> paramTypes) {
		return getFunctionVariable(this, name, paramTypes);
	}
	
	protected static Variable getFunctionVariable(Scope scope, String name, List<Type> paramTypes) {
		Variable rtn = null;
		
		for (Symbol s : scope.symbols) {
			if (s instanceof Variable && ((Variable)s).type instanceof FunctionType && s.name.equals(name)) {
				Variable v = (Variable)s;
				FunctionType functionType = (FunctionType)v.type;
				if (functionType.matchParameterTypes(paramTypes)) {
					rtn = v;
					break;
				}
			}
		}

		return rtn;
	}

	protected Class getClass(String name) {
		return getClass(this, name);
	}
	
	protected static Class getClass(Scope scope, String name) {
		for (Symbol s : scope.symbols) {
			if (s instanceof Class && s.name.equals(name)) {
				return (Class)s;
			}
		}
		
		return null;
	}
	
	protected boolean containsSymbol(Symbol s) {
		return symbols.contains(s);
	}
	
	@Override
	public String toString() {
		return "Scope: " + name;
	}

}
