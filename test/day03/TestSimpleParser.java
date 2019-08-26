public class TestSimpleParser {
	public static void main(String[] args) {
        SimpleParser parser = new SimpleParser();
        String script = null;
        ASTNode tree = null;

        try {
            script = "int age = 45+2; age= 20; age+10*2;";
            System.out.println("解析："+script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        try {
            script = "2+3+;";
            System.out.println("解析："+script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            script = "2+3*;";
            System.out.println("解析："+script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
        	script = "2+3;";
        	System.out.println("解析: " + script);
        	tree = parser.parse(script);
        	parser.dumpAST(tree, "");
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        }
	}
}