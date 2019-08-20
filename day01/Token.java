public interface Token {
	/**
	 * @return Token Type
	 */
	public TokenType getType();

	/**
	 * @return Token Value
	 */
	public String getText();
}