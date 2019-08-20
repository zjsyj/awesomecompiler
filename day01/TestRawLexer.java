public class TestRawLexer {
	public static void main(String[] args) {

		RawLexer lexer = new RawLexer();

        String script = "int age = 45;";
        System.out.println("parse :" + script);
        RawTokenReader tokenReader = lexer.tokenize(script);
        lexer.dump(tokenReader);

        script = "inta age = 45;";
        System.out.println("\nparse :" + script);
        tokenReader = lexer.tokenize(script);
        lexer.dump(tokenReader);

        script = "in age = 45;";
        System.out.println("\nparse :" + script);
        tokenReader = lexer.tokenize(script);
        lexer.dump(tokenReader);

        script = "age >= 45;";
        System.out.println("\nparse :" + script);
        tokenReader = lexer.tokenize(script);
        lexer.dump(tokenReader);

        script = "age > 45;";
        System.out.println("\nparse :" + script);
        tokenReader = lexer.tokenize(script);
        lexer.dump(tokenReader);
	}
}