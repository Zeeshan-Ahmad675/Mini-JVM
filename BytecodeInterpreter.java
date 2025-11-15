import java.io.*;
import java.util.*;

public class BytecodeInterpreter {
    public static void main(String[] args) throws IOException {
        Interpreter interpreter = new Interpreter(args[0]);
        int result = interpreter.run();
        System.out.println("Result = " + result);
    }
}


class Interpreter {
    private byte[] bytecode;
    private int pc = 0;


    private StackFrame currentFrame;
    private static Stack<StackFrame> callStack = new Stack<>();


    public Interpreter(String fileName) throws IOException{
        this.bytecode = ClassFileParser.main(new String[] { fileName, "main" });
        this.currentFrame = new StackFrame(10, 100);
    }


    public int run() {
        while (true) {
            int opcode = fetchByte();


            switch (opcode) {
                case 0x00: // nop
                    break;

                case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07: case 0x08: // iconst_m1 to iconst_5
                    currentFrame.push(opcode - 0x03);
                    break;

                case 0x10: // bipush
                    int val = fetchSignedByte();
                    currentFrame.push(val);
                    break;

                case 0x11: { // sipush
                    int valShort = fetchShort();
                    currentFrame.push(valShort);
                    break;
                }

                // Local variable load
                case 0x15: { // iload
                    int index = fetchByte();
                    currentFrame.push(currentFrame.getLocal(index));
                    break;
                }
                case 0x1a: case 0x1b: case 0x1c: case 0x1d: { // iload_0 .. iload_3
                    int index = opcode - 0x1a;
                    currentFrame.push(currentFrame.getLocal(index));
                    break;
                }

                // Local variable store
                case 0x36: { // istore
                    int index = fetchByte();
                    currentFrame.setLocal(index, currentFrame.pop());
                    break;
                }
                case 0x3b: case 0x3c: case 0x3d: case 0x3e: { // istore_0 .. istore_3
                    int index = opcode - 0x3b;
                    currentFrame.setLocal(index, currentFrame.pop());
                    break;
                }

                case 0x57: // pop
                    currentFrame.pop();
                    break;

                case 0x59: { // dup
                    int valTop = currentFrame.pop();
                    currentFrame.push(valTop);
                    currentFrame.push(valTop);
                    break;
                }

                // Arithmetic
                case 0x60: { // iadd
                    int v2 = currentFrame.pop();
                    int v1 = currentFrame.pop();
                    currentFrame.push(v1 + v2);
                    break;
                }
                case 0x64: { // isub
                    int v2 = currentFrame.pop();
                    int v1 = currentFrame.pop();
                    currentFrame.push(v1 - v2);
                    break;
                }
                case 0x68: { // imul
                    int v2 = currentFrame.pop();
                    int v1 = currentFrame.pop();
                    currentFrame.push(v1 * v2);
                    break;
                }
                case 0x6c: { // idiv
                    int v2 = currentFrame.pop();
                    int v1 = currentFrame.pop();
                    currentFrame.push(v1 / v2);
                    break;
                }

                // Branching (simple unconditional jump)
                case 0xa7: { // goto
                    int offset = fetchShort();
                    pc += offset - 3; // -3 because 3 bytes read (1 opcode + 2 offset)
                    break;
                }

                // Conditional branching example (equals zero)
                case 0x99: { // ifeq
                    int offset = fetchShort();
                    int value = currentFrame.pop();
                    if (value == 0) {
                        pc += offset - 3;
                    }
                    break;
                }
                case 0x9a: { // ifne
                    int offset = fetchShort();
                    int value = currentFrame.pop();
                    if (value != 0) {
                        pc += offset - 3;
                    }
                    break;
                }

                // Object creation
                case 0xbb: { // new (with hardcoded field count for example)
                    int objId = Memory.heap.newObject(2);
                    currentFrame.push(objId);
                    break;
                }

                // Field ops
                case 0xb4: { // getfield
                    int fieldIndex = fetchShort();
                    int objRef = currentFrame.pop();
                    int fieldValue = Memory.heap.getField(objRef, fieldIndex);
                    currentFrame.push(fieldValue);
                    break;
                }
                case 0xb5: { // putfield
                    int fieldIndex = fetchShort();
                    int value = currentFrame.pop();
                    int objRef = currentFrame.pop();
                    Memory.heap.putField(objRef, fieldIndex, value);
                    break;
                }

                // Method invocation
                case 0xb6: { // invokevirtual (simplified stub)
                    // For demonstration, pop arguments and just simulate call
                    // Actual implementation requires call stack handling and method lookup
                    int objRef = currentFrame.pop();
                    // Simulate method call result 0 pushed onto stack
                    currentFrame.push(0);
                    break;
                }
                case 0xb7: { // invokespecial (constructor or private method)
                    // Simplified stub similar to invokevirtual
                    int objRefOrArg = currentFrame.pop();
                    currentFrame.push(0);
                    break;
                }
                case 0xb8: { // invokestatic
                    // Simplified stub for static method
                    currentFrame.push(0);
                    break;
                }

                // Return
                case 0xac: { // ireturn
                    int retVal = currentFrame.pop();
                    if (callStack.isEmpty()) {
                        return retVal;
                    } else {
                        // Pop frame etc. (not implemented)
                    }
                    break;
                }
                case 0xb1: { // return (void)
                    if (callStack.isEmpty()) {
                        return 0;
                    } else {
                        // Pop frame etc. (not implemented)
                    }
                    break;
                }

                default:
                    // throw new RuntimeException("Unsupported opcode: 0x" + Integer.toHexString(opcode));
            }
        }
    }


    private int fetchByte() {
        return bytecode[pc++] & 0xFF;
    }


    private int fetchSignedByte() {
        int val = bytecode[pc++];
        return val > 127 ? val - 256 : val;
    }


    private int fetchShort() {
        int high = bytecode[pc++] & 0xFF;
        int low = bytecode[pc++] & 0xFF;
        int val = (high << 8) | low;
        return val > 32767 ? val - 65536 : val;
    }
}


class StackFrame {
    private int[] locals;
    private int[] operandStack;
    private int sp = -1;


    public StackFrame(int localsSize, int stackSize) {
        locals = new int[localsSize];
        operandStack = new int[stackSize];
    }


    public void push(int val) {
        operandStack[++sp] = val;
    }


    public int pop() {
        if (sp < 0) throw new RuntimeException("Stack underflow");
        return operandStack[sp--];
    }


    public int getLocal(int index) {
        return locals[index];
    }


    public void setLocal(int index, int value) {
        locals[index] = value;
    }
}


