/**
 * token stream reader where Parser get the Tokens from
 */
public interface TokenReader {
	/**
	 * @return return next token in the stream and move next, null if stream is empty
	 */
	public Token read();

	/**
	 * @return  return next token in the stream, that's all, null if empty
	 */
	public Token peek();

	/**
	 * step back, recover from last Token
	 */
	public void unread();

	/**
	 * @return get the current position of stream
	 */
	public int getPosition();

	/**
	 * @param position set the current position of stream
	 */
	public void setPosition(int position);
}