package play;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NameSpace extends BlockScope {
	private NameSpace parent = null;
	private List<NameSpace> subNameSpace = new LinkedList<>();
	private String name = null;
	
	protected NameSpace(String name, Scope enclosingScope, ParserRuleContext ctx) {
		this.name = name;
		this.enclosingScope = enclosingScope;
		this.ctx = ctx;
	}
	
	public String getName() {
		return name;
	}
	
	public List<NameSpace> subNameSpace() {
		return Collections.unmodifiableList(subNameSpace);
	}
	
	public void addSubNameSpace(NameSpace child) {
		child.parent = this;
		subNameSpace.add(child);
	}
	
	public void removeSubNameSpace(NameSpace child) {
		child.parent = null;
		subNameSpace.remove(child);
	}
	
}
