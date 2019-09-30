package play;

public class PlayScript {

	public static void main(String[] args) {
		System.out.println(">Starting Play Script Interpreter");
		
		ASTEvaluator ast = new ASTEvaluator(null);

		System.out.println(ast.getClass().getName());
	}

}
