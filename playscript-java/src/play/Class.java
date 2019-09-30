package play;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

public class Class extends Scope implements Type {
	private Class parentClass = null;
	
	protected Class(String name, ParserRuleContext ctx) {
		this.name = name;
		this.ctx = ctx;
		
		thisRef = new This(this, ctx);
		thisRef.type = this;
	}
	
	protected Class getParentClass() {
		return this.parentClass;
	}
	
	protected void setParentClass(Class theClass) {
		parentClass = theClass;
		
		superRef = new Super(parentClass, ctx);
		superRef.type = parentClass;
	}
	
	private This thisRef = null;
	private Super superRef = null;
	private static Class rootClass = new Class("Object", null);
	
	public This getThis() {
		return thisRef;
	}
	
	public Super getSuper() {
		return superRef;
	}
	
	@Override
	public String toString() {
		return "Class " + name;
	}
	
	protected Variable getVariable(String name) {
		Variable rtn = super.getVariable(name);
		
		if (rtn == null && parentClass != null) {
			rtn = parentClass.getVariable(name);
		}
		
		return rtn;
	}
	
	@Override
	protected Class getClass(String name) {
		Class rtn = super.getClass(name);
		
		if (rtn == null && parentClass != null) {
			rtn = parentClass.getClass(name);
		}
		
		return rtn;
	}
	
	protected Function findContrutor(List<Type> paramTypes) {
		Function rtn = super.getFunction(name, paramTypes);
		
		return rtn;
	}
	
	protected Function getFunction(String name, List<Type> paramTypes) {
		Function rtn =super.getFunction(name, paramTypes);
		
		if (rtn == null && parentClass != null) {
			rtn = parentClass.getFunction(name, paramTypes);
		}
		
		return rtn;
	}
	
	protected Variable getFunctionVariable(String name, List<Type> paramTypes) {
		Variable rtn = super.getFunctionVariable(name, paramTypes);
		
		if (rtn == null && parentClass != null) {
			rtn = parentClass.getFunctionVariable(name, paramTypes);
		}
		
		return rtn;
	}
	
	@Override
	public boolean isType(Type type) {
		if (this == type) return true;
		
		if (type instanceof Class) {
			return ((Class)type).isAncestor(this);
		}
		
		return false;
	}
	
	public boolean isAncestor(Class theClass) {
		if (theClass.getParentClass() != null) {
			if (theClass.getParentClass() == this)
				return true;
			else
				return isAncestor(theClass.getParentClass());
		}
		
		return false;
	}
}
