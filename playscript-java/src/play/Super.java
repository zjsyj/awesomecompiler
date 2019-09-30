package play;

import org.antlr.v4.runtime.ParserRuleContext;

public class Super extends Variable {
	Super(Class theClass, ParserRuleContext ctx) {
		super("super", theClass, ctx);
	}
	
	private Class Class() {
		return (Class)enclosingScope;
	}
}
