package play;

import java.util.HashMap;
import java.util.Map;

public class PlayObject {
	protected Map<Variable, Object> fields = new HashMap<>();
	
	public Object getValue(Variable variable) {
		Object rtn = fields.get(variable);
		
		//TODO how to get value of parent class fields
		
		if (rtn == null) {
			rtn = NullObject.instance();
		}
		
		return rtn;
	}
	
	public void setValue(Variable variable, Object value) {
		fields.put(variable, value);
	}

}
