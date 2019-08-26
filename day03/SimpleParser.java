import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleParser {
	public ASTNode parse(String script) throws Exception {
		RawLexer lexer = new RawLexer();
		TokenReader tokens = lexer.tokenize(script);
		ASTNode rootNode = prog(tokens);
		return rootNode;
	}

	private SimpleASTNode prog(TokenReader tokens) throws Exception {
		SimpleASTNode node = new SimpleASTNode(ASTNodeType.Program, "parse");

		while (tokens.peek() != null) {
			SimpleASTNode child = intDeclare(tokens);

			if (child == null) {
				child = expressionStatment(tokens);
			}

			if (child == null) {
				child = assignmentStatement(tokens);
			}

			if (child != null) {
				node.addChild(child);
			}
			else {
				throw new Exception("unkown statement");
			}
		}

		return node;
	}

	private SimpleASTNode expressionStatment(TokenReader tokens) throws Exception {
		int pos = tokens.getPosition();
		SimpleASTNode node = additive(tokens);
		if (node != null) {
			Token token = tokens.peek();
			if (token != null && token.getType() == TokenType.SemiColon) {
				tokens.read();
			}
			else {
				node = null;
				tokens.setPosition(pos);
			}
		}

		return node;
	}

	private SimpleASTNode assignmentStatement(TokenReader tokens) throws Exception {
		SimpleASTNode node = null;
		Token token = tokens.peek();

		if (token != null && token.getType() == TokenType.Identifier) {
			token = tokens.read();
			node = new SimpleASTNode(ASTNodeType.AssignmentStmt, token.getText());
			token = tokens.peek();
			if (token != null && token.getType() == TokenType.Assignment) {
				tokens.read();
				SimpleASTNode child = additive(tokens);
				if (child == null) {
					throw new Exception("invalid assignment  statement, expecting an expression");
				}
				else {
					node.addChild(child);
					token = tokens.peek();
					if (token != null && token.getType() == TokenType.SemiColon) {
						tokens.read();
					}
					else {
						throw new Exception("invalid statement, expecting semicolon");
					}
				}
			}
			else {
				tokens.unread();
				node = null;
			}
		}

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
					if (child == null) {
						throw new Exception("invalid variable initialized, expecting an expression");
					}
					else {
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
					throw new Exception("invalid statement, SemiColon expected");
				}
			}
		}

		return node;
	}

	private SimpleASTNode additive(TokenReader tokens) throws Exception {
		SimpleASTNode child1 = multiplicative(tokens);
		SimpleASTNode node = child1;

		if (child1 != null) {
			while (true) {
				Token token = tokens.peek();
				if (token != null && (token.getType() == TokenType.Plus || token.getType() == TokenType.Minus)) {
					token = tokens.read();
					SimpleASTNode child2 = multiplicative(tokens);
					if (child2 != null) {
						node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
						node.addChild(child1);
						node.addChild(child2);
						child1 = node;
					}
					else {
						throw new Exception("invalid additive expression, expecting right part");
					}
				}
				else {
					break;
				}
			}
		}

		return node;
	}

	private SimpleASTNode multiplicative(TokenReader tokens) throws Exception {
		SimpleASTNode child1 = primary(tokens);
		SimpleASTNode node = child1;

		while (true) {
			Token token = tokens.peek();
			if (token != null && (token.getType() == TokenType.Star || token.getType() == TokenType.Slash)) {
				token = tokens.read();
				SimpleASTNode child2 = primary(tokens);
				if (child2 != null) {
					node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
					node.addChild(child1);
					node.addChild(child2);
					child1 = node;
				}
				else {
					throw new Exception("invalid multiplicative expression, expecting right part");
				}
			}
			else {
				break;
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
					throw new Exception("expecting an additive expression inside parenthesis");
				}
			}
		}

		return node;
	}

	private class SimpleASTNode implements ASTNode {
		SimpleASTNode parent = null;
		List<ASTNode> children = new ArrayList<ASTNode>();
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

		public void addChild(SimpleASTNode child)  {
			children.add(child);
			child.parent = this;
		}
	}

	void dumpAST(ASTNode node, String indent) {
		System.out.println(indent + node.getType() + " " + node.getText());
		for (ASTNode child : node.getChildren()) {
			dumpAST(child, indent + "\t");
		}
	}
}