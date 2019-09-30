package play;

public final class NullObject extends ClassObject {
	private static NullObject instance = new NullObject();
	
	private NullObject() {
		
	}
	
	public static NullObject instance() {
		return instance;
	}
	
	@Override
	public String toString() {
		return "Null";
	}

}
