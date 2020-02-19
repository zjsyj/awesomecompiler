package play;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;

import java.util.HashSet;
import java.util.Set;


public class ClosureAnalyzer {
	AnnotatedTree at = null;
	
	public ClosureAnalyzer(AnnotatedTree at) {
		this.at = at;
	}
	
	public void analyzeClosures() {
		for (Type type : at.types) {
			if (type instanceof Function && !((Function)type).isMethod()) {
                Set set = calcClosureVariables((Function)type);
				if (set.size() > 0) {
					((Function)type).closureVariables = set;
				}
			}
		}
	}
	
	private Set<Variable> calcClosureVariables(Function function) {
		Set<Variable> refered = variablesReferedByScope(function);
		Set<Variable> declared = variablesDeclaredUnderScope(function);

		refered.removeAll(declared);
		
		return refered;
	}
	
	private Set<Variable> variablesReferedByScope(Scope scope) {
		Set<Variable> rtn = new HashSet<>();
		
		ParserRuleContext scopeNode = scope.ctx;
		
		for (ParserRuleContext node : at.symbolOfNode.keySet()) {
			Symbol symbol = at.symbolOfNode.get(node);
			if (symbol instanceof Variable && isAncestor(scopeNode, node)) {
				rtn.add((Variable)symbol);
			}
		}
		
		return rtn;
	}
	
	private boolean isAncestor(RuleContext node1, RuleContext node2) {
		if (node2.parent == null) {
			return false;
		}
		else if (node2.parent == node1) {
			return true;
		}
		else {
			return isAncestor(node1, node2.parent);
		}
	}
	
	private Set<Variable> variablesDeclaredUnderScope(Scope scope) {
		Set<Variable> rtn = new HashSet<>();
		
		for (Symbol symbol : scope.symbols) {
			if (symbol instanceof Variable) {
				rtn.add((Variable)symbol);
			}
			else if (symbol instanceof Scope) {
				rtn.addAll(variablesDeclaredUnderScope((Scope)symbol));
			}
		}
		return rtn;
	}
}
