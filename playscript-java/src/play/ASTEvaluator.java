package play;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import play.PlayScriptParser.*;

/**
 * AST interpreter, with help of AnnotatedTree, on the level
 * of AST, executing the play scripts
 *
 **/
public class ASTEvaluator extends PlayScriptBaseVisitor<Object> {
	private AnnotatedTree at = null;
	
	public ASTEvaluator(AnnotatedTree at) {
		this.at = at;
	}
	
	protected boolean traceStackFrame = false;
	protected boolean traceFunctionCall = false;
	
	// management of stack frame
	private Stack<StackFrame> stack = new Stack<>();
	
	private void pushStack(StackFrame frame) {
		if (stack.size() > 0) {
			for (int i = stack.size()-1; i > 0; i--) {
				StackFrame f = stack.get(i);
				
				if (f.scope.enclosingScope == frame.scope.enclosingScope) {
					frame.parentFrame = f.parentFrame;
					break;
				}
				else if (f.scope == frame.scope.enclosingScope) {
					frame.parentFrame = f;
					break;
				}
				else if (frame.object instanceof FunctionObject) {
					FunctionObject functionObject = (FunctionObject)frame.object;
					
					if (functionObject.receiver != null && functionObject.receiver.enclosingScope == f.scope) {
						frame.parentFrame = f;
					}
				}
			}
			
			if (frame.parentFrame == null) {
				frame.parentFrame = stack.peek();
			}
		}
		
		stack.push(frame);
		if (traceStackFrame) {
			dumpStackFrame();
		}
	}
	
	private void popStack() {
		stack.pop();
	}
	
	private void dumpStackFrame() {
		System.out.println("\nStack Frames ------------");
		for (StackFrame frame : stack) {
			System.out.println(frame);
		}
		System.out.println("----------------------------\n");
	}
	
	public LValue getLValue(Variable variable) {
		StackFrame f = stack.peek();
		
		PlayObject valueContainer = null;
		while (f != null) {
			if (f.scope.containsSymbol(variable)) {
				valueContainer = f.object;
				break;
			}
			f = f.parentFrame;
		}
		
		if (valueContainer == null) {
			f = stack.peek();
			while (f != null) {
				if (f.contains(variable)) {
					valueContainer = f.object;
					break;
				}
				f = f.parentFrame;
			}
		}
		
		MyLValue lvalue = new MyLValue(valueContainer, variable);
		return lvalue;
	}
	
	private ClassObject firstClassObjectInStack() {
		for (int i = stack.size()-1; i > 0; i--) {
			StackFrame stackFrame = stack.get(i);
			if (stackFrame.object instanceof ClassObject) {
				return (ClassObject)stackFrame.object;
			}
		}
		
		return null;
	}
	
	private void getClosureValues(Function function, PlayObject valueContainer) {
		if (function.closureVariables != null) {
			for (Variable var : function.closureVariables) {
				LValue lvalue = getLValue(var);
				Object value = lvalue.getValue();
				valueContainer.fields.put(var, value);
			}
		}
	}
	
	private void getClosureValues(ClassObject classObject) {
		PlayObject tempObject = new PlayObject();
		
		for (Variable v : classObject.fields.keySet()) {
			if (v.type instanceof FunctionType) {
				Object object = classObject.fields.get(v);
				if (object != null) {
					FunctionObject functionObject = (FunctionObject)object;
					getClosureValues(functionObject.function, tempObject);
				}
			}
		}
		
		classObject.fields.putAll(tempObject.fields);
	}
	
	private final class MyLValue implements LValue {
		private Variable variable;
		private PlayObject valueContainer;
		
		public MyLValue(PlayObject valueContainer, Variable variable) {
			this.valueContainer = valueContainer;
			this.variable = variable;
		}
		
		@Override
		public Object getValue() {
			if (variable instanceof This || variable instanceof Super) {
				return valueContainer;
			}
			
			return valueContainer.getValue(variable);
		}
		
		@Override
		public void setValue(Object value) {
			valueContainer.setValue(variable, value);
			
			if (value instanceof FunctionObject) {
				((FunctionObject)value).receiver = (Variable)variable;
			}
		}
		
		@Override
		public Variable getVariable() {
			return variable;
		}
		
		@Override
		public PlayObject getValueContainer() {
			return valueContainer;
		}
		
		@Override
		public String toString() {
			return "LValue of " + variable.getName() + "is : " + getValue();
		}
	}
	
	protected ClassObject createAndInitClassObject(Class theClass) {
		ClassObject obj = new ClassObject();
		obj.type = theClass;
		
		Stack<Class> ancesterChain = new Stack<>();
		
		ancesterChain.push(theClass);
		while (theClass.getParentClass() != null) {
			ancesterChain.push(theClass.getParentClass());
			theClass = theClass.getParentClass();
		}
		
		StackFrame frame = new StackFrame(obj);
		pushStack(frame);
		while (ancesterChain.size() > 0) {
			Class c = ancesterChain.pop();
			defaultObjectInit(c, obj);
		}
		popStack();
		
		return obj;
	}
	
	protected void defaultObjectInit(Class theClass, ClassObject obj) {
		for (Symbol symbol : theClass.symbols) {
			if (symbol instanceof Variable) {
				obj.fields.put((Variable)symbol, null);
			}
		}
		
		ClassBodyContext ctx = ((ClassDeclarationContext)theClass.ctx).classBody();
		visitClassBody(ctx);
	}
	
	private void println(FunctionCallContext ctx) {
		if (ctx.expressionList() != null) {
			Object value = visitExpressionList(ctx.expressionList());
			if (value instanceof LValue) {
				value = ((LValue)value).getValue();
			}
			System.out.println(value);
		}
		else {
			System.out.println();
		}
	}
	
	private Object add(Object obj1, Object obj2, Type targetType) {
		Object rtn = null;
		
		if (targetType == PrimitiveType.String) {
			rtn = String.valueOf(obj1) + String.valueOf(obj2);
		}
		else if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() + ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
			rtn = ((Number)obj1).floatValue() + ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() + ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() + ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() + ((Number)obj2).shortValue();
		}
		else {
			System.out.println("unsupported add operation");
		}
		
		return rtn;
	}
	
	private Object minus(Object obj1, Object obj2, Type targetType) {
		Object rtn = null;

		if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() - ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
				rtn = ((Number)obj1).floatValue() - ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() - ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() - ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() - ((Number)obj2).shortValue();
		}
		
		return rtn;
	}
	
	private Object mul(Object obj1, Object obj2, Type targetType) {
		Object rtn = null;
		if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() * ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
			rtn = ((Number)obj1).floatValue() * ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() * ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() * ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() * ((Number)obj2).shortValue();
		}
		
		return rtn;
	}
	
	private Object div(Object obj1, Object obj2, Type targetType) {
		Object rtn = null;

		if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() / ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
			rtn = ((Number)obj1).floatValue() / ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() / ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() / ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() / ((Number)obj2).shortValue();
		}
		
		return rtn;
	}
	
	private Boolean EQ(Object obj1, Object obj2, Type targetType) {
		Boolean rtn = null;
		if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() == ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
			rtn = ((Number)obj1).floatValue() == ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() == ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() == ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() == ((Number)obj2).shortValue();
		}
		else {
			rtn = obj1 == obj2;
		}

		return rtn;
	}
	
	private Object GE(Object obj1, Object obj2, Type targetType) {
		Object rtn = null;

		if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() >= ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
			rtn = ((Number)obj1).floatValue() >= ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() >= ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() >= ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() >= ((Number)obj2).shortValue();
		}
		
		return rtn;
	}
	
	private Object GT(Object obj1, Object obj2, Type targetType) {
		Object rtn = null;

		if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() > ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
			rtn = ((Number)obj1).floatValue() > ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() > ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() > ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() > ((Number)obj2).shortValue();
		}
		
		return rtn;
	}
	
	private Object LE(Object obj1, Object obj2, Type targetType) {
		Object rtn = null;

		if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() <= ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
			rtn = ((Number)obj1).floatValue() <= ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() <= ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() <= ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() <= ((Number)obj2).shortValue();
		}
		
		return rtn;
	}
	
	private Object LT(Object obj1, Object obj2, Type targetType) {
		Object rtn = null;

		if (targetType == PrimitiveType.Integer) {
			rtn = ((Number)obj1).intValue() < ((Number)obj2).intValue();
		}
		else if (targetType == PrimitiveType.Float) {
			rtn = ((Number)obj1).floatValue() < ((Number)obj2).floatValue();
		}
		else if (targetType == PrimitiveType.Long) {
			rtn = ((Number)obj1).longValue() < ((Number)obj2).longValue();
		}
		else if (targetType == PrimitiveType.Double) {
			rtn = ((Number)obj1).doubleValue() < ((Number)obj2).doubleValue();
		}
		else if (targetType == PrimitiveType.Short) {
			rtn = ((Number)obj1).shortValue() < ((Number)obj2).shortValue();
		}

		return rtn;
	}
	
	@Override
	public Object visitBlock(BlockContext ctx) {
		BlockScope scope = (BlockScope)at.node2Scope.get(ctx);
		if (scope != null) {
			StackFrame frame = new StackFrame(scope);
			pushStack(frame);
		}
		
		Object rtn = visitBlockStatements(ctx.blockStatements());
		if (scope != null) {
			popStack();
		}
		
		return rtn;
	}
	
	@Override
	public Object visitBlockStatement(BlockStatementContext ctx) {
		Object rtn = null;
		
		if (ctx.variableDeclarators() != null) {
			rtn = visitVariableDeclarators(ctx.variableDeclarators());
		}
		else if (ctx.statement() != null) {
			rtn = visitStatement(ctx.statement());
		}
		
		return rtn;
	}
	
	@Override
	public Object visitEnhancedForControl(EnhancedForControlContext ctx) {
		return super.visitEnhancedForControl(ctx);
	}
	
	@Override
	public Object visitExpression(ExpressionContext ctx) {
		Object rtn = null;
		
		if (ctx.bop != null && ctx.expression().size() >= 2) {
			Object left = visitExpression(ctx.expression(0));
			Object right = visitExpression(ctx.expression(1));
			
			Object leftObject = left;
			Object rightObject = right;
			
			if (left instanceof LValue) {
				leftObject = ((LValue) left).getValue();
			}
			
			if (right instanceof LValue) {
				rightObject = ((LValue) right).getValue();
			}
			
			Type type = at.typeOfNode.get(ctx);
			Type type1 = at.typeOfNode.get(ctx.expression(0));
			Type type2 = at.typeOfNode.get(ctx.expression(1));
			
			switch (ctx.bop.getType()) {
				case PlayScriptParser.ADD:
					rtn = add(leftObject, rightObject, type);
					break;
				case PlayScriptParser.SUB:
					rtn = minus(leftObject, rightObject, type);
					break;
				case PlayScriptParser.MUL:
					rtn = mul(leftObject, rightObject, type);
					break;
				case PlayScriptParser.DIV:
					rtn = div(leftObject, rightObject, type);
					break;
				case PlayScriptParser.EQUAL:
					rtn = EQ(leftObject, rightObject, PrimitiveType.getUpperType(type1, type2));
					break;
				case PlayScriptParser.NOTEQUAL:
					rtn = !EQ(leftObject, rightObject, PrimitiveType.getUpperType(type1, type2));
					break;
				case PlayScriptParser.LE:
					rtn = LE(leftObject, rightObject, PrimitiveType.getUpperType(type1, type2));
					break;
				case PlayScriptParser.LT:
					rtn = LT(leftObject, rightObject, PrimitiveType.getUpperType(type1, type2));
					break;
				case PlayScriptParser.GE:
					rtn = GE(leftObject, rightObject, PrimitiveType.getUpperType(type1, type2));
					break;
				case PlayScriptParser.GT:
					rtn = GT(leftObject, rightObject, PrimitiveType.getUpperType(type1, type2));
					break;
				case PlayScriptParser.AND:
					rtn = (Boolean)leftObject && (Boolean)rightObject;
					break;
				case PlayScriptParser.OR:
					rtn = (Boolean)leftObject || (Boolean)rightObject;
					break;
				case PlayScriptParser.ASSIGN:
					if (leftObject instanceof LValue) {
						((LValue)left).setValue(rightObject);
						rtn = right;
					}
					else {
						System.out.println("unsupported feature during assignment");
					}
					break;
				default:
					break;
			}
		}
		else if (ctx.bop != null && ctx.bop.getType() == PlayScriptParser.DOT) {
			Object leftObject = visitExpression(ctx.expression(0));
			if (leftObject instanceof LValue) {
				Object value = ((LValue)leftObject).getValue();
				if (value instanceof ClassObject) {
					ClassObject valueContainer = (ClassObject)value;
					Variable leftVar = (Variable)at.symbolOfNode.get(ctx.expression(0));
					if (ctx.IDENTIFIER() != null) {
						Variable variable = (Variable)at.symbolOfNode.get(ctx);
						
						if (!(leftVar instanceof This || leftVar instanceof Super)) {
							variable = at.lookupVariable(valueContainer.type, variable.getName());
						}
						LValue lValue = new MyLValue(valueContainer, variable);
						rtn = lValue;
					}
					else if (ctx.functionCall() != null) {
						if (traceFunctionCall) {
							System.out.println("\n>>MethodCall: " + ctx.getText());
						}
						
						rtn = methodCall(valueContainer, ctx.functionCall(), (leftVar instanceof Super));
					}
				}
				else {
					System.out.println("Expecting an Object Reference");
				}
			}
			else if (ctx.primary() != null) {
				rtn = visitPrimary(ctx.primary());
			}
			else if (ctx.postfix != null) {
				Object value = visitExpression(ctx.expression(0));
				LValue lvalue = null;
				Type type = at.typeOfNode.get(ctx.expression(0));
				if (value instanceof LValue) {
					lvalue = ((LValue) value);
					value = lvalue.getValue();
				}
				switch (ctx.postfix.getType()) {
					case PlayScriptParser.INC:
						if (type == PrimitiveType.Integer) {
							lvalue.setValue((Integer)value + 1);
						}
						else {
							lvalue.setValue((Long)value + 1);
						}
						rtn = value;
						break;
					case PlayScriptParser.DEC:
						if (type == PrimitiveType.Integer) {
							lvalue.setValue((Integer)value - 1);
						}
						else {
							lvalue.setValue((long)value - 1);
						}
						rtn = value;
						break;
					default:
						break;
				}
			}
			else if (ctx.prefix != null) {
				Object value = visitExpression(ctx.expression(0));
				LValue lvalue = null;
				Type type = at.typeOfNode.get(ctx.expression(0));
				if (value instanceof LValue) {
					lvalue = (LValue)value;
					value = lvalue.getValue();
				}
				
				switch (ctx.prefix.getType()) {
					case PlayScriptParser.INC:
						if (type == PrimitiveType.Integer) {
							rtn = ((Integer)value + 1);
						}
						else {
							rtn = (Long)value + 1;
						}
						lvalue.setValue(rtn);
						break;
					case PlayScriptParser.DEC:
						if (type == PrimitiveType.Integer) {
							rtn = (Integer)value - 1;
						}
						else {
							rtn = (Long)value - 1;
						}
						lvalue.setValue(rtn);
						break;
					case PlayScriptParser.BANG:
						rtn = !((Boolean)value);
						break;
					default:
						break;
				}
			}
			else if (ctx.functionCall() != null) {
				rtn = visitFunctionCall(ctx.functionCall());
			}
		}
		
		return rtn;
	}
	
	@Override
	public Object visitExpressionList(ExpressionListContext ctx) {
		Object rtn = null;
		
		for (ExpressionContext child : ctx.expression()) {
			rtn = visitExpression(child);
		}

		return rtn;
	}
	
	@Override
	public Object visitForInit(ForInitContext ctx) {
		Object rtn = null;
		
		if (ctx.variableDeclarators() != null) {
			rtn = visitVariableDeclarators(ctx.variableDeclarators());
		}
		else if (ctx.expressionList() != null) {
			rtn = visitExpressionList(ctx.expressionList());
		}
		
		return rtn;
	}
	
	@Override
	public Object visitLiteral(LiteralContext ctx) {
		Object rtn = null;
		
		if (ctx.integerLiteral() != null) {
			rtn = visitIntegerLiteral(ctx.integerLiteral());
		}
		else if (ctx.floatLiteral() != null) {
			rtn = visitFloatLiteral(ctx.floatLiteral());
		}
		else if (ctx.BOOL_LITERAL() != null) {
			if (ctx.BOOL_LITERAL().getText().equals("true")) {
				rtn = Boolean.TRUE;
			}
			else {
				rtn = Boolean.FALSE;
			}
		}
		else if (ctx.STRING_LITERAL() != null) {
			String withQuotationMark = ctx.STRING_LITERAL().getText();
			rtn  = withQuotationMark.substring(1, withQuotationMark.length() - 1);
		}
		else if (ctx.CHAR_LITERAL() != null) {
			rtn = ctx.CHAR_LITERAL().getText().charAt(0);
		}
		else if (ctx.NULL_LITERAL() != null) {
			rtn = NullObject.instance();
		}
		
		return rtn;
	}
	
	@Override
	public Object visitIntegerLiteral(IntegerLiteralContext ctx) {
		Object rtn = null;
		
		if (ctx.DECIMAL_LITERAL() != null) {
			rtn = Integer.valueOf(ctx.DECIMAL_LITERAL().getText());
		}
		
		return rtn;
	}
	
	@Override
	public Object visitFloatLiteral(FloatLiteralContext ctx) {		
		return Float.valueOf(ctx.getText());
	}
	
	@Override
	public Object visitParExpression(ParExpressionContext ctx) {
		return visitExpression(ctx.expression());
	}
	
	@Override
	public Object visitPrimary(PrimaryContext ctx) {
		Object rtn = null;
		
		if (ctx.literal() != null) {
			rtn = visitLiteral(ctx.literal());
		}
		else if (ctx.IDENTIFIER() != null) {
			Symbol symbol = at.symbolOfNode.get(ctx);
			
			if (symbol instanceof Variable) {
				rtn = getLValue((Variable) symbol);
			}
			else if (symbol instanceof Function) {
				FunctionObject obj = new FunctionObject((Function) symbol);
				rtn = obj;
			}
		}
		else if (ctx.expression() != null) {
			rtn = visitExpression(ctx.expression());
		}
		else if (ctx.THIS() != null) {
			This thisRef = (This)at.symbolOfNode.get(ctx);
			rtn = getLValue(thisRef);
		}
		else if (ctx.SUPER() != null) {
			Super superRef = (Super)at.symbolOfNode.get(ctx);
			rtn = getLValue(superRef);
		}
		
		return rtn;
	}
	
	@Override
	public Object visitPrimitiveType(PrimitiveTypeContext ctx) {
		Object rtn = null;
		
		if (ctx.INT() != null) {
			rtn = PlayScriptParser.INT;
		}
		else if (ctx.LONG() != null) {
			rtn = PlayScriptParser.LONG;
		}
		else if (ctx.FLOAT() != null) {
			rtn = PlayScriptParser.FLOAT;
		}
		else if (ctx.DOUBLE() != null) {
			rtn = PlayScriptParser.DOUBLE;
		}
		else if (ctx.BOOLEAN() != null) {
			rtn = PlayScriptParser.BOOLEAN;
		}
		else if (ctx.CHAR() != null) {
			rtn = PlayScriptParser.CHAR;
		}
		else if (ctx.SHORT() != null) {
			rtn = PlayScriptParser.SHORT;
		}
		else if (ctx.BYTE() != null) {
			rtn = PlayScriptParser.BYTE;
		}

		return rtn;
	}
	
	@Override
	public Object visitStatement(StatementContext ctx) {
		Object rtn = null;
		
		if (ctx.statementExpression != null) {
			rtn = visitExpression(ctx.statementExpression);
		}
		else if (ctx.IF() != null) {
			Boolean condition = (Boolean)visitParExpression(ctx.parExpression());
			if (Boolean.TRUE == condition) {
				rtn = visitStatement(ctx.statement(0));
			}
			else if (ctx.ELSE() != null) {
				rtn = visitStatement(ctx.statement(1));
			}
		}
		else if (ctx.WHILE() != null) {
			if (ctx.parExpression().expression() != null && ctx.statement(0)!= null) {
				while (true) {
					Boolean condition = true;
					Object value = visitExpression(ctx.parExpression().expression());
					if (value instanceof LValue) {
						condition = (Boolean)((LValue) value).getValue();
					}
					else {
						condition = (Boolean)value;
					}
					
					if (condition) {
						if (condition) {
							rtn = visitStatement(ctx.statement(0));
							
							if (rtn instanceof BreakObject) {
								rtn = null;
								break;
							}
							else if (rtn instanceof ReturnObject) {
								break;
							}
						}
					}
					else {
						break;
					}
				}
			}
		}
		else if (ctx.FOR() != null) {
			BlockScope scope = (BlockScope)at.node2Scope.get(ctx);
			StackFrame frame = new StackFrame(scope);
			
			pushStack(frame);
			
			ForControlContext forControl = ctx.forControl();
			if (forControl.enhancedForControl() != null) {
				
			}
			else {
				if (forControl.forInit() != null) {
					rtn = visitForInit(forControl.forInit());
				}
				
				while (true) {
					Boolean condition = true;
					if (forControl.expression() != null) {
						Object value = visitExpression(forControl.expression());
						if (value instanceof LValue) {
							condition = (Boolean)((LValue) value).getValue();
						}
						else {
							condition = (Boolean)value;
						}
					}
					
					if (condition) {
						rtn = visitStatement(ctx.statement(0));
						
						if (rtn instanceof BreakObject) {
							rtn = null;
							break;
						}
						else if (rtn instanceof ReturnObject) {
							break;
						}
						
						if (forControl.forUpdate != null) {
							visitExpressionList(forControl.forUpdate);
						}
					}
					else {
						break;
					}
				}
			}
			
			popStack();
		}
		else if (ctx.blockLabel != null) {
			rtn = visitBlock(ctx.blockLabel);
		}
		else if (ctx.BREAK() != null) {
			rtn = BreakObject.instance();
		}
		else if (ctx.RETURN() != null) {
			if (ctx.expression() != null) {
				rtn = visitExpression(ctx.expression());
				
				if (rtn instanceof LValue) {
					rtn = ((LValue)rtn).getValue();
				}
				
				if (rtn instanceof FunctionObject) {
					FunctionObject functionObject = (FunctionObject)rtn;
					getClosureValues(functionObject.function, functionObject);
				}
				else if (rtn instanceof ClassObject) {
					ClassObject classObject = (ClassObject)rtn;
					getClosureValues(classObject);
				}
			}
			
			rtn = new ReturnObject(rtn);
		}
		
		return rtn;
	}
	
	@Override
	public Object visitVariableDeclarator(VariableDeclaratorContext ctx) {
		Object rtn = null;
		LValue lvalue = (LValue)visitVariableDeclaratorId(ctx.variableDeclaratorId());
		
		if (ctx.variableInitializer() != null) {
			rtn = visitVariableInitializer(ctx.variableInitializer());
			if (rtn instanceof LValue) {
				rtn = ((LValue) rtn).getValue();
			}
			
			lvalue.setValue(rtn);
		}
		
		return rtn;
	}
	
	@Override
	public Object visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
		Object rtn = null;
		
		Symbol symbol = at.symbolOfNode.get(ctx);
		rtn = getLValue((Variable) symbol);
		
		return rtn;
	}
	
	@Override
	public Object visitVariableDeclarators(VariableDeclaratorsContext ctx)  {
		Object rtn = null;
		
		for (VariableDeclaratorContext child : ctx.variableDeclarator()) {
			rtn = visitVariableDeclarator(child);
		}
		
		return rtn;
	}
	
	@Override
	public Object visitVariableInitializer(VariableInitializerContext ctx) {
		Object rtn = null;
		
		if (ctx.expression() != null) {
			rtn = visitExpression(ctx.expression());
		}

		return rtn;
	}
	
	@Override
	public Object visitBlockStatements(BlockStatementsContext ctx) {
		Object rtn = null;
		
		for (BlockStatementContext child : ctx.blockStatement()) {
			rtn = visitBlockStatement(child);
			
			if (rtn instanceof BreakObject) {
				break;
			}
			else if (rtn instanceof ReturnObject) {
				break;
			}
		}
		
		return rtn;
	}
	
	@Override
	public Object visitProg(ProgContext ctx) {
		Object rtn = null;
		
		pushStack(new StackFrame((BlockScope) at.node2Scope.get(ctx)));
		
		rtn = visitBlockStatements(ctx.blockStatements());
		
		popStack();
		return rtn;
	}
	
	@Override
	public Object visitFormalParameter(FormalParameterContext ctx) {
		return super.visitFormalParameter(ctx);
	}
	
	@Override
	public Object visitFormalParameterList(FormalParameterListContext ctx) {
		return super.visitFormalParameterList(ctx);
	}
	
	@Override
	public Object visitFormalParameters(FormalParametersContext ctx) {
		return super.visitFormalParameters(ctx);
	}
	
	@Override
	public Object visitFunctionCall(FunctionCallContext ctx) {
		if (ctx.THIS() != null) {
			thisContructor(ctx);
			return null;
		}
		else if (ctx.SUPER() != null) {
			thisContructor(ctx);
			return null;
		}
		
		Object rtn = null;
		
		String functionName = ctx.IDENTIFIER().getText();
		Symbol symbol = at.symbolOfNode.get(ctx);
		
		if (symbol instanceof Class) {
			return createAndInitClassObject((Class) symbol);
		}
		else if (functionName.equals("println")) {
			println(ctx);
			return rtn;
		}
		
		FunctionObject functionObject = getFunctionObject(ctx);
		Function function = functionObject.function;
		
		if (function.isContructor()) {
			Class theClass = (Class)function.enclosingScope;
			ClassObject newObject = createAndInitClassObject(theClass);
			
			methodCall(newObject, ctx, false);
			
			return newObject;
		}
		
		List<Object> paramValues = calcParamValues(ctx);
		
		if (traceFunctionCall) {
			System.out.println("\n>>Function Call: " + ctx.getText());
		}
		
		rtn = functionCall(functionObject, paramValues);
		
		return rtn;
	}
	
	private List<Object> calcParamValues(FunctionCallContext ctx) {
		List<Object> paramValues = new LinkedList<>();
		
		if (ctx.expressionList() != null) {
			for (ExpressionContext exp : ctx.expressionList().expression()) {
				Object value = visitExpression(exp);
				if (value instanceof LValue) {
					value = ((LValue)value).getValue();
				}
				
				paramValues.add(value);
			}
		}
		
		return paramValues;
	}
	
	private FunctionObject getFunctionObject(FunctionCallContext ctx) {
		if (ctx.IDENTIFIER() == null) return null;
		
		FunctionObject functionObject = null;
		Function function = null;
		
		Symbol symbol = at.symbolOfNode.get(ctx);
		
		if (symbol instanceof Variable) {
			Variable variable = (Variable)symbol;
			LValue lvalue = getLValue(variable);
			Object value = lvalue.getValue();
			
			if (value instanceof FunctionObject) {
				functionObject = (FunctionObject)value;
				function = functionObject.function;
			}
		}
		else if (symbol instanceof Function) {
			function = (Function)symbol;
		}
		else {
			String functionName = ctx.IDENTIFIER().getText();
			at.log("unable to find function or function variable " + functionName, ctx);
			return null;
		}
		
		if (functionObject == null) {
			functionObject = new FunctionObject(function);
		}

		return functionObject;
	}
	
	private Object functionCall(FunctionObject functionObject, List<Object> paramValues) {
		Object rtn = null;
		
		StackFrame functionFrame = new StackFrame(functionObject);
		pushStack(functionFrame);
		
		FunctionDeclarationContext functionCode = (FunctionDeclarationContext)functionObject.function.ctx;
		if (functionCode.formalParameters().formalParameterList() != null) {
			for (int i = 0; i < functionCode.formalParameters().formalParameterList().formalParameter().size(); i++) {
				FormalParameterContext param = functionCode.formalParameters().formalParameterList().formalParameter(i);
				LValue lvalue = (LValue)visitVariableDeclaratorId(param.variableDeclaratorId());
				lvalue.setValue(paramValues.get(i));
			}
		}
		
		rtn = visitFunctionDeclaration(functionCode);
		popStack();
		
		if (rtn instanceof ReturnObject) {
			rtn = ((ReturnObject)rtn).returnValue;
		}

		return rtn;
	}
	
	private Object methodCall(ClassObject classObject, FunctionCallContext ctx, Boolean isSuper) {
		Object rtn = null;
		
		StackFrame classFrame = new StackFrame(classObject);
		pushStack(classFrame);
		
		FunctionObject functionObject = getFunctionObject(ctx);
		
		popStack();
		
		Function function = functionObject.function;
		Class theClass = classObject.type;
		
		if (!function.isContructor() && !isSuper) {
			Function overrided = theClass.getFunction(function.name, function.getParamTypes());
			if (overrided != null && overrided != function) {
				function = overrided;
				functionObject.setFunction(function);
			}
		}
		
		List<Object> paramValues = calcParamValues(ctx);
		
		pushStack(classFrame);
		rtn = functionCall(functionObject, paramValues);
		popStack();
		
		return rtn;
	}
	
	private void thisContructor(FunctionCallContext ctx) {
		Symbol symbol = at.symbolOfNode.get(ctx);
		
		if (symbol instanceof Class) {
			return;
		}
		else if (symbol instanceof Function) {
			Function function = (Function)symbol;
			FunctionObject functionObject = new FunctionObject(function);
			List<Object> paramValues = calcParamValues(ctx);
			
			functionCall(functionObject, paramValues);
		}
	}
	
	@Override
	public Object visitFunctionDeclaration(FunctionDeclarationContext ctx) {
		return visitFunctionBody(ctx.functionBody());
	}
	
	@Override
	public Object visitFunctionBody(FunctionBodyContext ctx) {
		Object rtn = null;
		
		if (ctx.block() != null) {
			rtn = visitBlock(ctx.block());
		}
		
		return rtn;
	}
	
	@Override
	public Object visitQualifiedName(QualifiedNameContext ctx) {
		return super.visitQualifiedName(ctx);
	}
	
	@Override
	public Object visitQualifiedNameList(QualifiedNameListContext ctx) {
		return super.visitQualifiedNameList(ctx);
	}
	
	@Override
	public Object visitTypeTypeOrVoid(TypeTypeOrVoidContext ctx) {
		return super.visitTypeTypeOrVoid(ctx);
	}
	
	@Override
	public Object visitClassBody(ClassBodyContext ctx) {
		Object rtn = null;
		
		for (ClassBodyDeclarationContext child : ctx.classBodyDeclaration()) {
			rtn = visitClassBodyDeclaration(child);
		}
		
		return rtn;
	}
	
	@Override
	public Object visitClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
		Object rtn = null;
		
		if (ctx.memberDeclaration() != null) {
			rtn = visitMemberDeclaration(ctx.memberDeclaration());
		}
		
		return rtn;
	}
	  @Override
	    public Object visitMemberDeclaration(MemberDeclarationContext ctx) {
	        Object rtn = null;
	        if (ctx.fieldDeclaration() != null) {
	            rtn = visitFieldDeclaration(ctx.fieldDeclaration());
	        }
	        return rtn;
	    }

	    @Override
	    public Object visitFieldDeclaration(FieldDeclarationContext ctx) {
	        Object rtn = null;
	        if (ctx.variableDeclarators() != null) {
	            rtn = visitVariableDeclarators(ctx.variableDeclarators());
	        }
	        return rtn;
	    }

	    @Override
	    public Object visitClassDeclaration(ClassDeclarationContext ctx) {
	        return super.visitClassDeclaration(ctx);
	    }

	    @Override
	    public Object visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
	        return super.visitConstructorDeclaration(ctx);
	    }

	    @Override
	    public Object visitCreator(CreatorContext ctx) {
	        return super.visitCreator(ctx);
	    }

	    @Override
	    public Object visitTypeArgument(TypeArgumentContext ctx) {
	        return super.visitTypeArgument(ctx);
	    }

	    @Override
	    public Object visitTypeList(TypeListContext ctx) {
	        return super.visitTypeList(ctx);
	    }

	    @Override
	    public Object visitVariableModifier(VariableModifierContext ctx) {
	        return super.visitVariableModifier(ctx);
	    }
	
}