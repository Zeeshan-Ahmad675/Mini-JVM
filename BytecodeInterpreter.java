import java.io.IOException;

public class BytecodeInterpreter {
    public static void main(String[] args) throws IOException {
        Interpreter interpreter = new Interpreter(args[0], "main");
        int result = interpreter.run();
        String str = ClassLoader.heap.getStringField(result);
        if (str == null) {
            System.out.println("Result = " + result);
        } else
            System.out.println("Result = " + str);
    }
}
