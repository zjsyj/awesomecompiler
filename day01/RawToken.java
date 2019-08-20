public final class RawToken implements Token {
	private TokenType type = null;
	private String text = null;

	public void setType(TokenType type) {
		this.type = type;
	}

	@Override
	public TokenType getType() {
		return type;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return text;
	}
}