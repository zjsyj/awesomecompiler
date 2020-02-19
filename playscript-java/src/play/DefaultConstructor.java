package play;

public class DefaultConstructor extends Function {
    protected DefaultConstructor(String name, Class theClass) {
        super(name, theClass, null);
    }

    public Class Class(){
        return (Class)enclosingScope;
    }
}
