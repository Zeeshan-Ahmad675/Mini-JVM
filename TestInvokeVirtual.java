public class TestInvokeVirtual {
    int a, b;
    public TestInvokeVirtual(int a, int b) {
      this.a = a;
      this.b = b;
    }
    public static int add(int a, int b) {
        return a + b;
    }

    public static int main(String[] args) {
        TestInvokeVirtual obj = new TestInvokeVirtual(2, 5);
        return TestInvokeVirtual.add(5, 4);
    }
}
