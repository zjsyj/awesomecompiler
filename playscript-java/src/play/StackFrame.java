package play;

public class StackFrame {
	Scope scope = null;
	
	StackFrame parentFrame = null;
	
	PlayObject object = null;
	
	protected boolean contains(Variable variable) {
		if (object != null && object.fields != null) {
			return object.fields.containsKey(variable);
		}
		
		return false;
	}
	
	public StackFrame(BlockScope scope) {
		this.scope = scope;
		this.object = new PlayObject();
	}
	
	public StackFrame(ClassObject object) {
		this.scope = object.type;
		this.object = object;
	}
	
	public StackFrame(FunctionObject object) {
		this.scope = object.function;
		this.object = object;
	}
	
	@Override
	public String toString() {
		String rtn = "" + scope;
		if (parentFrame != null) {
			rtn += " ->" + parentFrame;
		}
		
		return rtn;
	}
	
	

}
