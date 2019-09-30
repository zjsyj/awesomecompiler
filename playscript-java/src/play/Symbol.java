package play;

import org.antlr.v4.runtime.ParserRuleContext;

public abstract class Symbol {
	//symbol name
	protected String name = null;
	
	protected Scope enclosingScope = null;
	
	protected int visibility = 0;
	
	protected ParserRuleContext ctx = null;
	
	public String getName() {
		return name;
	}
	
	public Scope getEnclosingScope() {
		return enclosingScope;
	}
}
