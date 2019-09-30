package play;

import java.util.List;

public interface FunctionType extends Type {
	public Type getReturnType();
	
	public List<Type> getParamTypes();
	
	public boolean matchParameterTypes(List<Type> paramTypes);
}
