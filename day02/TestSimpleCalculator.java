public class TestSimpleCalculator {
    public static void main(String[] args) {
        SimpleCaculator calculator = new SimpleCaculator();
        String script = null;

        //测试表达式
        script = "2+3*5";
        System.out.println("\n计算: " + script + "，看上去一切正常。");
        calculator.evaluate(script);

        //测试语法错误
        script = "2+";
        System.out.println("\n: " + script + "，应该有语法错误。");
        calculator.evaluate(script);

        script = "2+3+4";
        System.out.println("\n计算: " + script + "，结合性出现错误。");
        calculator.evaluate(script);

        script = "(2+4)*(2+5)";
        System.out.println("\nCalculating: " + script);
        calculator.evaluate(script);
    }
}