import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RawLexer {
	/**
	 *@param code [one line of code]
	 *@return construct a series of tokens
	 */
	public RawTokenReader tokenize(String code) {
		tokens = new ArrayList<Token>();
		CharArrayReader reader = new CharArrayReader(code.toCharArray());
		tokenText = new StringBuffer();
		token = new RawToken();

		int ich = 0;
		char ch = 0;
		DfaState state = DfaState.Initial;

		try {
			while ((ich = reader.read()) != -1) {
				ch = (char) ich;

				switch (state) {
					case Initial:
						state = initToken(ch);
						break;
					case Id:
						if (isAlpha(ch) || isDigit(ch)) {
							tokenText.append(ch);
						}
						else {
							state = initToken(ch);
						}
						break;
					case GT:
						if (ch == '=') {
							token.setType(TokenType.GE);
							state = DfaState.GE;
							tokenText.append(ch);
						}
						else {
							state = initToken(ch);
						}
						break;
					case GE:
					case Assignment:
					case Plus:
					case Minus:
					case Star:
					case Slash:
					case SemiColon:
					case LeftParen:
					case RightParen:
						state = initToken(ch);
						break;
					case IntLiteral:
						if (isDigit(ch)) {
							tokenText.append(ch);
						}
						else {
							state = initToken(ch);
						}
						break;
					case Id_int1:
						if (ch == 'n') {
							state = DfaState.Id_int2;
							tokenText.append(ch);
						}
						else if (isDigit(ch) || isAlpha(ch)) {
							state = DfaState.Id;
							tokenText.append(ch);
						}
						else {
							state = initToken(ch);
						}
						break;
					case Id_int2:
						if (ch == 't') {
							state = DfaState.Id_int3;
							tokenText.append(ch);
						}
						else if (isDigit(ch) || isAlpha(ch)) {
							state = DfaState.Id;
							tokenText.append(ch);
						}
						else {
							state = initToken(ch);
						}
						break;
					case Id_int3:
						if (isBlank(ch)) {
							token.setType(TokenType.Int);
							state = initToken(ch);
						}
						else {
							state = DfaState.Id;
							tokenText.append(ch);
						}
						break;
					default:
						break;
				}
			}

			if (tokenText.length() > 0)  {
				initToken(ch);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return new RawTokenReader(tokens);
	}

	public static void dump(RawTokenReader tokenReader) {
		System.out.println("text\t\ttype");
		Token token = null;

		while ((token = tokenReader.read()) != null) {
			System.out.println(token.getText()+"\t\t"+token.getType());
		}
	}

	private StringBuffer tokenText = null;
	private List<Token> tokens = null;
	private RawToken token = null;

	private boolean isAlpha(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
	}

	private boolean isDigit(char ch) {
		return ch >= '0' && ch <= '9';
	}

	private boolean isBlank(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\n';
	}

	private enum DfaState {
		Initial,

		If, Id_if1, Id_if2, Else, Id_else1, Id_else2, Id_else3, Id_else4, Int, Id_int1, Id_int2, Id_int3,

		Id, GE, GT,
		Assignment,

		Plus, Minus, Star, Slash,

		SemiColon,
		LeftParen,
		RightParen,

		IntLiteral,
	}

	private DfaState initToken(char ch) {
		if (tokenText.length() > 0) {
			token.setText(tokenText.toString());
			tokens.add(token);

			tokenText = new StringBuffer();
			token = new RawToken();
		}

		DfaState newState = DfaState.Initial;
		if (isAlpha(ch)) {
			if (ch == 'i') {
				newState = DfaState.Id_int1;
			}
			else {
				newState = DfaState.Id;
			}
			token.setType(TokenType.Identifier);
			tokenText.append(ch);
		}
		else if (isDigit(ch)) {
			newState = DfaState.IntLiteral;
			token.setType(TokenType.IntLiteral);
			tokenText.append(ch);
		}
		else if (ch == '>') {
			newState = DfaState.GT;
			token.setType(TokenType.GT);
			tokenText.append(ch);
		}
		else if (ch == '+') {
			newState = DfaState.Plus;
			token.setType(TokenType.Plus);
			tokenText.append(ch);
		}
		else if (ch == '*') {
			newState = DfaState.Star;
			token.setType(TokenType.Star);
			tokenText.append(ch);
		}
		else if (ch == '-') {
			newState = DfaState.Minus;
			token.setType(TokenType.Minus);
			tokenText.append(ch);
		}
		else if (ch == '/') {
			newState = DfaState.Slash;
			token.setType(TokenType.Slash);
			tokenText.append(ch);
		}
		else if (ch == '(') {
			newState = DfaState.LeftParen;
			token.setType(TokenType.LeftParen);
			tokenText.append(ch);
		}
		else if (ch == ')') {
			newState = DfaState.RightParen;
			token.setType(TokenType.RightParen);
			tokenText.append(ch);
		}
		else if (ch == '=') {
			newState = DfaState.Assignment;
			token.setType(TokenType.Assignment);
			tokenText.append(ch);
		}
		else {
			newState = DfaState.Initial;
		}

		return newState;
	}
}