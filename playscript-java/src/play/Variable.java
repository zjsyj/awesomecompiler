package play;

import org.antlr.v4.runtime.ParserRuleContext;

public class Variable extends Symbol {
	protected Type type = null;
	
	protected Object defaultValue = null;
	
	protected Integer multiplicity = 1;
	
	protected Variable(String name, Scope enclosingScope, ParserRuleContext ctx) {
		this.name = name;
		this.enclosingScope = enclosingScope;
		this.ctx = ctx;
	}
	
	public boolean isClassMember() {
		return enclosingScope instanceof Class;
	}
	
	@Override
	public String toString() {
		return "Variable " + name + " ->" + type;
	}
}
