package play;

public interface LValue {
	public Object getValue();
	public void setValue(Object value);
	public Variable getVariable();
	public PlayObject getValueContainer();
}
