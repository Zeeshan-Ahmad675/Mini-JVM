public class StackFrame {
  private int[] locals;
  private int[] operandStack;
  private String className;
  private String methodName;
  private int sp = -1;
  private int returnPc;

  public StackFrame(int localsSize, int stackSize, String className, String methodName) {
    locals = new int[localsSize];
    operandStack = new int[stackSize];
    this.className = className;
    this.methodName = methodName;
  }

  public void push(int val) {
    operandStack[++sp] = val;
  }

  public int pop() {
    if (sp < 0)
      throw new RuntimeException("Stack underflow");
    return operandStack[sp--];
  }

  public int getLocal(int index) {
    return locals[index];
  }

  public void setLocal(int index, int value) {
    locals[index] = value;
  }

  public void setReturnPc(int pc) {
    this.returnPc = pc;
  }

  public int getReturnPc() {
    return this.returnPc;
  }

  public String getClassName() {
    return this.className;
  }

  public String getMethodName() {
    return this.methodName;
  }
}
