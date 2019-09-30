package play;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DefaultFunctionType implements FunctionType {
	protected String name = null;
	protected Scope enclosingScope = null;
	protected Type returnType = null;
	protected List<Type> paramTypes = new LinkedList<>();
	
	private static int nameIndex = 1;
	
	public DefaultFunctionType() {
		name = "FunctionType"  + nameIndex++;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Scope getEnclosingScope() {
		return enclosingScope;
	}
	
	@Override
	public Type getReturnType() {
		return returnType;
	}
	
	@Override
	public List<Type> getParamTypes() {
		return Collections.unmodifiableList(paramTypes);
	}
	
	@Override
	public String toString() {
		return "FunctionType";
	}
	
	@Override
	public boolean isType(Type type) {
		if (type instanceof FunctionType) {
			return isType(this, (FunctionType)type);
		}
		
		return false;
	}
	
	public static boolean isType(FunctionType type1, FunctionType type2) {
		if (type1 == type2) return true;
		
		if (!type1.getReturnType().isType(type2.getReturnType())) {
			return false;
		}
		
		List<Type> paramType1 = type1.getParamTypes();
		List<Type> paramType2 = type2.getParamTypes();
		
		if (paramType1.size() != paramType2.size())
			return false;
		
		for (int i = 0; i < paramType1.size(); i++) {
			if (!paramType1.get(i).isType(paramType2.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean matchParameterTypes(List<Type> paramTypes) {
		if (this.paramTypes.size() != paramTypes.size()) {
			return false;
		}
        for (int i = 0; i < paramTypes.size(); i++) {
            Type type1 = this.paramTypes.get(i);
            Type type = paramTypes.get(i);
            if (!type1.isType(type)) {
            	return false;
            }
        }
        
		return true;
	}
}
