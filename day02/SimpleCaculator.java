import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleCaculator {
	/**
	 * @param script
	 */
	public void evaluate(String script) {
		try {
			ASTNode tree = parse(script);

			dumpAST(tree, "");
			evaluate(tree, "");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public ASTNode parse(String code) throws Exception {
		RawLexer lexer = new RawLexer();
		TokenReader tokens = lexer.tokenize(code);

		ASTNode rootNode = prog(tokens);

		return rootNode;
	}

	private SimpleASTNode prog(TokenReader tokens) throws Exception {
		SimpleASTNode node = new SimpleASTNode(ASTNodeType.Program, "SimpleCaculator");
		SimpleASTNode child = additive(tokens);

		if (child != null)
			node.addChild(child);

		return node;
	}

	private SimpleASTNode intDeclare(TokenReader tokens) throws Exception {
		SimpleASTNode node = null;
		Token token = tokens.peek();

		if (token != null && token.getType() == TokenType.Int) {
			token = tokens.read();
			if (tokens.peek().getType() == TokenType.Identifier) {
				token = tokens.read();
				node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
				token = tokens.peek();
				if (token != null && token.getType() == TokenType.Assignment) {
					tokens.read();
					SimpleASTNode child = additive(tokens);
					if (child == null)
						throw new Exception("invalid variable initialization, expecting an expression");
					else
						node.addChild(child);
				}
			}
		}
		else {
			throw new Exception("variable name expected");
		}

		if (node != null) {
			token = tokens.peek();
			if (token != null && token.getType() == TokenType.SemiColon) {
				tokens.read();
			}
			else {
				throw new Exception("invalid statement, semicolon expected");
			}
		}

		return node;
	}

	private SimpleASTNode additive(TokenReader tokens) throws Exception {
		SimpleASTNode child1 = multiplicative(tokens);
		SimpleASTNode node = child1;

		Token token = tokens.peek();
		if (child1 != null && token != null) {
			if (token.getType() == TokenType.Plus || token.getType() == TokenType.Minus) {
				token = tokens.read();
				SimpleASTNode child2 = additive(tokens);
				if (child2 != null) {
					node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
					node.addChild(child1);
					node.addChild(child2);
				}
				else {
					throw new Exception("invalid additive expression, expecting right part");
				}
			}
		}

		return node;
	}

	private SimpleASTNode multiplicative(TokenReader tokens) throws Exception {
		SimpleASTNode child1 = primary(tokens);
		SimpleASTNode node = child1;

		Token token = tokens.peek();
		if (child1 != null && token != null) {
			if (token.getType() == TokenType.Star || token.getType() == TokenType.Slash) {
				token = tokens.read();
				SimpleASTNode child2 = primary(tokens);
				if (child2 != null) {
					node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
					node.addChild(child1);
					node.addChild(child2);
				}
				else {
					throw new Exception("invalid multiplicative expression, expecting right part");
				}
			}
		}

		return node;
	}

	private SimpleASTNode primary(TokenReader tokens) throws Exception {
		SimpleASTNode node = null;
		Token token = tokens.peek();

		if (token != null) {
			if (token.getType() == TokenType.IntLiteral) {
				token = tokens.read();
				node = new SimpleASTNode(ASTNodeType.IntLiteral, token.getText());
			}
			else if (token.getType() == TokenType.Identifier) {
				token = tokens.read();
				node = new SimpleASTNode(ASTNodeType.Identifier, token.getText());
			}
			else if (token.getType() == TokenType.LeftParen) {
				tokens.read();
				node = additive(tokens);
				if (node != null) {
					token = tokens.peek();
					if (token != null && token.getType() == TokenType.RightParen) {
						tokens.read();
					}
					else {
						throw new Exception("expecting right parenthesis");
					}
				}
				else {
					throw new Exception("Expecting an additive expression inside parenthesis");
				}
			}
		}

		return node;
	}

	private int evaluate(ASTNode node, String indent) {
		int result = 0;

		System.out.println(indent + "Caculating: " + node.getType());

		switch (node.getType()) {
			case Program:
				for (ASTNode child : node.getChildren()) {
					result = evaluate(child, indent + '\t');
				}
				break;

			case Additive:
				ASTNode child1 = node.getChildren().get(0);
				int     value1 = evaluate(child1, indent + "\t");
				ASTNode child2 = node.getChildren().get(1);
				int     value2 = evaluate(child2, indent + "\t");

				if (node.getText().equals("+"))
					result = value1 + value2;
				else
					result = value1 - value2;
				break;

			case Multiplicative:
				child1 = node.getChildren().get(0);
				value1 = evaluate(child1, indent + "\t");
				child2 = node.getChildren().get(1);
				value2 = evaluate(child2, indent + "\t");

				if (node.getText().equals("*"))
					result = value1 * value2;
				else
					result = value1 / value2;
				break;

			case IntLiteral:
				result = Integer.valueOf(node.getText()).intValue();
				break;
			default:
				break;
		}

		System.out.println(indent +"result: " + result);
		return result;
	}

	private void dumpAST(ASTNode node, String indent) {
		System.out.println(indent + node.getType() + " " + node.getText());
		for (ASTNode child : node.getChildren()) {
			dumpAST(child, indent + "\t");
		}
	}

	private class SimpleASTNode implements ASTNode {
		SimpleASTNode parent = null;
		List<ASTNode> children = new ArrayList<>();
		List<ASTNode> readonlyChildren = Collections.unmodifiableList(children);
		ASTNodeType nodeType = null;
		String text = null;

		public SimpleASTNode(ASTNodeType nodeType, String text) {
			this.nodeType = nodeType;
			this.text = text;
		}

		@Override
		public ASTNode getParent() {
			return parent;
		}

		@Override
		public List<ASTNode> getChildren() {
			return readonlyChildren;
		}

		@Override
		public ASTNodeType getType() {
			return nodeType;
		}

		@Override
		public String getText() {
			return text;
		}

		public void addChild(SimpleASTNode child) {
			children.add(child);
			child.parent = this;
		}
	}
}