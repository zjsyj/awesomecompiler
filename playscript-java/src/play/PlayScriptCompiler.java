package play;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class PlayScriptCompiler {
	AnnotatedTree at = null;
	PlayScriptLexer lexer = null;
	PlayScriptParser parser = null;
	
	public AnnotatedTree compile(String script, boolean verbose, boolean ast_dump) {
		at = new AnnotatedTree();
		
		
		lexer = new PlayScriptLexer(CharStreams.fromString(script));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		parser = new PlayScriptParser(tokens);
		at.ast = parser.prog();
		
		ParseTreeWalker walker = new ParseTreeWalker();
		TypeAndScopeScanner pass1 = new TypeAndScopeScanner(at);
		walker.walk(pass1, at.ast);
		
		TypeResolver pass2 = new TypeResolver(at);
		walker.walk(pass2, at.ast);
		
		RefResolver pass3 = new RefResolver(at);
		walker.walk(pass3, at.ast);
		
		TypeChecker pass4 = new TypeChecker(at);
		walker.walk(pass4, at.ast);
		
		SematicValidator pass5 = new SematicValidator(at);
		walker.walk(pass5, at.ast);
		
		ClosureAnalyzer closureAnalyzer = new ClosureAnalyzer(at);
		closureAnalyzer.analyzeClosures();
		
		if (verbose || ast_dump) {
			dumpAST();
		}
		
		if (verbose) {
			dumpSymbols();
		}
		
		return at;
	}
	
	public AnnotatedTree compile(String script) {
		return compile(script, false, false);
	}
	
	public void dumpSymbols() {
		if (at != null) {
			System.out.println(at.getScopeTreeString());
		}
	}
	
	public void dumpAST() {
		if (at != null) {
			System.out.println(at.ast.toStringTree(parser));
		}
	}
	
	public void dumpCompilationLogs() {
		if (at != null) {
			for (CompilationLog log : at.logs) {
				System.out.println(log);
			}
		}
	}
	
	public Object Execute(AnnotatedTree at) {
		ASTEvaluator visitor = new ASTEvaluator(at);
		Object result = visitor.visit(at.ast);
		
		return result;
	}
}
