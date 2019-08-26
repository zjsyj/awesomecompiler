import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * a simple script interpreter
 *  > 2+3;
 *  > int employId = 22;
 *  > int managerId;
 *  > managerId = employId;
 *  >
 *  > exit();
 */
public class SimpleScript {
	private HashMap<String, Integer>variables = new HashMap<>();
	private static boolean verbose = false;

	public static void run(String[] args) {
		if (args.length > 0 && args[0].equals("-v")) {
			verbose = true;
			System.out.println("verbose mode on");
		}

		System.out.println("Simple Script language!");

		SimpleParser parser = new SimpleParser();
		SimpleScript script = new SimpleScript();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String scriptText = "";
		System.out.print("\n>");

		while (true) {
			try {
				String line = reader.readLine().trim();
				if (line.equals("exit();")) {
					System.out.println("Bye");
					break;
				}

				scriptText += line + "\n";
				if (line.endsWith(";")) {
					ASTNode tree = parser.parse(scriptText);
					if (verbose) {
						parser.dumpAST(tree, "");
					}

					script.evaluate(tree, "");
					System.out.print("\n>");
					scriptText = "";
				}
			}
			catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
				System.out.print("\n>");
				scriptText = "";
			}
		}
	}

	private Integer evaluate(ASTNode node, String indent) throws Exception {
		Integer result = null;
		if (verbose) {
			System.out.println(indent + "Caculating: " + node.getType());
		}

		switch (node.getType()) {
			case Program:
				for (ASTNode child : node.getChildren()) {
					result = evaluate(child, indent);
				}
				break;
			case Additive:
				ASTNode child1 = node.getChildren().get(0);
				ASTNode child2 = node.getChildren().get(1);
				Integer value1 = evaluate(child1, indent + "\t");
				Integer value2 = evaluate(child2, indent + "\t");

				if (node.getText().equals("+")) {
					result = value1 + value2;
				}
				else {
					result = value1 - value2;
				}
				break;
			case Multiplicative:
				child1 = node.getChildren().get(0);
				child2 = node.getChildren().get(1);
				value1 = evaluate(child1, indent + "\t");
				value2 = evaluate(child2, indent + "\t");
				if (node.getText().equals("*")) {
					result = value1 * value2;
				}
				else {
					result = value1 / value2;
				}
				break;
			case IntLiteral:
				result = Integer.valueOf(node.getText()).intValue();
				break;
			case Identifier:
				String varName = node.getText();
				if (variables.containsKey(varName)) {
					Integer value = variables.get(varName);
					if (value != null) {
						result = value.intValue();
					}
					else {
						throw new Exception("variable " + varName + " has not been initialized");
					}
				}
				else {
					throw new Exception("unkown variable: " + varName);
				}
				break;
			case AssignmentStmt:
				varName = node.getText();
				if (!variables.containsKey(varName)) {
					throw new Exception("unkown variable: " + varName);
				}
			case IntDeclaration:
				varName = node.getText();
				Integer varValue = null;
				if (node.getChildren().size() > 0) {
					ASTNode child = node.getChildren().get(0);
					result = evaluate(child, indent + "\t");
					varValue = Integer.valueOf(result);
				}
				variables.put(varName, varValue);
				break;
			default:
		}

		if (verbose) {
			System.out.println(indent + "Result: " + result);
		}
		else if (indent.equals("")) {
			if (node.getType() == ASTNodeType.IntDeclaration || node.getType() == ASTNodeType.AssignmentStmt) {
				System.out.println(node.getText() + ": " + result);
			}
			else if (node.getType() != ASTNodeType.Program) {
				System.out.println(result);
			}
		}

		return result;
	}
}