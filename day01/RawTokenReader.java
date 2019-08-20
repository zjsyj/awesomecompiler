import java.util.List;

public class RawTokenReader implements TokenReader {
	List<Token> tokens = null;
	int pos = 0;

	public RawTokenReader(List<Token> tokens) {
		this.tokens = tokens;
	}

	@Override
	public Token read() {
		if (pos < tokens.size()) {
			return tokens.get(pos++);
		}

		return null;
	}

	@Override
	public Token peek() {
		if (pos < tokens.size()) {
			return tokens.get(pos);
		}

		return null;
	}

	@Override
	public void unread() {
		if (pos > 0)
			pos -= 1;
	}

	@Override
	public int getPosition() {
		return pos;
	}

	@Override
	public void setPosition(int position) {
		if (position >= 0 && position < tokens.size()) {
			pos = position;
		}
	}
}