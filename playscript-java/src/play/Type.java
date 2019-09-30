package play;

public interface Type {
	public String getName();
	
	public Scope getEnclosingScope();
	
	/**
	 * this type is the target type
	 */
	public boolean isType(Type type);

}
