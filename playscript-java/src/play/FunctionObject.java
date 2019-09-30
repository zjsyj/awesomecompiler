package play;

public class FunctionObject extends PlayObject {
	protected Function function = null;
	
	protected Variable receiver = null;
	
	public FunctionObject (Function function) {
		this.function = function;
	}
	
	protected void setFunction(Function function) {
		this.function = function;
	}
}
