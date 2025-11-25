import java.io.*;
import java.util.*;

public class BytecodeInterpreter {
    public static void main(String[] args) throws IOException {
        Interpreter interpreter = new Interpreter(args[0], "main");
        int result = interpreter.run();
        System.out.println("Result = " + result);
    }
}


class Interpreter {
    private byte[] bytecode;
    private int pc = 0;


    private StackFrame currentFrame;
    private static Stack<StackFrame> callStack = new Stack<>();


    public Interpreter(String fileName, String method) throws IOException{
        if(!ClassLoader.heap.isClassLoaded(fileName)) ClassLoader.loadClass(fileName);
        ClassFileParser.Code_attribute cattr = ClassLoader.heap.getMethodCode(fileName, method);
        this.bytecode = cattr.code;
        this.currentFrame = new StackFrame(cattr.max_locals, cattr.max_stack, fileName, method);
    }


    public int run() throws IOException{
        while (true) {
            int opcode = fetchByte();
            System.out.printf("Opcode: 0x%02x%n", opcode);

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

                case 0x2a: case 0x2b: case 0x2c: case 0x2d: { // aload_0 .. aload_3
                    int index = opcode - 0x2a;
                    currentFrame.push(currentFrame.getLocal(index));
                    break;
                }

                case 0x4b: case 0x4c: case 0x4d: case 0x4e: { // astore_0 .. astore_3
                    int index = opcode - 0x4b;
                    currentFrame.setLocal(index, currentFrame.pop());
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

                case 0x9f: { // if_icmpeq
                    int offset = fetchShort();
                    int value2 = currentFrame.pop();
                    int value1 = currentFrame.pop();
                    if (value1 == value2) {
                        pc += offset - 3;
                    }
                    break;
                }
                case 0xa0: { // if_icmpne
                    int offset = fetchShort();
                    int value2 = currentFrame.pop();
                    int value1 = currentFrame.pop();
                    if (value1 != value2) {
                        pc += offset - 3;
                    }
                    break;
                }
                case 0xa1: { // if_icmplt
                    int offset = fetchShort();
                    int value2 = currentFrame.pop();
                    int value1 = currentFrame.pop();
                    if (value1 < value2) {
                        pc += offset - 3;
                    }
                    break;
                }
                case 0xa2: { // if_icmpge
                    int offset = fetchShort();
                    int value2 = currentFrame.pop();
                    int value1 = currentFrame.pop();
                    if (value1 >= value2) {
                        pc += offset - 3;
                    }
                    break;
                }
                case 0xa3: { // if_icmpgt
                    int offset = fetchShort();
                    int value2 = currentFrame.pop();
                    int value1 = currentFrame.pop();
                    if (value1 > value2) {
                        pc += offset - 3;
                    }
                    break;
                }
                case 0xa4: { // if_icmple
                    int offset = fetchShort();
                    int value2 = currentFrame.pop();
                    int value1 = currentFrame.pop();
                    if (value1 <= value2) {
                        pc += offset - 3;
                    }
                    break;
                }

                // Object creation
                case 0xbb: { // new
                    int indexbyte1 = fetchByte();
                    int indexbyte2 = fetchByte();
                    int name_index = (((ClassFileParser.CONSTANT_Class_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), indexbyte1 << 8 |indexbyte2)).name_index);
                    int objId = ClassLoader.heap.newObject((String.valueOf(((ClassFileParser.CONSTANT_Utf8_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), name_index)).bytes)));
                    currentFrame.push(objId);
                    break;
                }

                // Field ops
                case 0xb4: { // getfield
                    int fieldIndex = fetchShort();
                    int objRef = currentFrame.pop();
                    int name_and_type_idx = ((ClassFileParser.CONSTANT_Fieldref_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), fieldIndex)).name_and_type_index;

                    String fname = String.valueOf(getConstantUTF8(((ClassFileParser.CONSTANT_NameAndType_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), name_and_type_idx)).name_index));
                    String ftype = String.valueOf(getConstantUTF8(((ClassFileParser.CONSTANT_NameAndType_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), name_and_type_idx)).descriptor_index));

                    switch(ftype){
                        case "I":{
                                int field = ClassLoader.heap.getIntField(objRef, fname);
                                currentFrame.push(field);
                            }
                            break;
                        case "F":{
                                int field = ClassLoader.heap.getIntField(objRef, fname);
                                currentFrame.push(field);
                            }
                            break;
                        case "J":{
                                long field = ClassLoader.heap.getLongField(objRef, fname);
                                currentFrame.push((int)(field >> 32));
                                currentFrame.push((int)field);
                            }
                            break;
                        case "D":{
                                long field = ClassLoader.heap.getLongField(objRef, fname);
                                currentFrame.push((int)(field >> 32));
                                currentFrame.push((int)field);
                            }
                            break;
                    }
                    break;
                }
                case 0xb5: { // putfield
                    int fieldIndex = fetchShort();
                    int value = currentFrame.pop();
                    // int objRef = currentFrame.pop();

                    int name_and_type_idx = ((ClassFileParser.CONSTANT_Fieldref_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), fieldIndex)).name_and_type_index;
                    String fname = String.valueOf(getConstantUTF8(((ClassFileParser.CONSTANT_NameAndType_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), name_and_type_idx)).name_index));
                    String ftype = String.valueOf(getConstantUTF8(((ClassFileParser.CONSTANT_NameAndType_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), name_and_type_idx)).descriptor_index));

                    switch(ftype){
                        case "I":{
                                int objRef = currentFrame.pop();
                                ClassLoader.heap.putIntField(objRef, fname, value);
                            }
                            break;
                        case "F":{
                                int objRef = currentFrame.pop();
                                ClassLoader.heap.putIntField(objRef, fname, value);
                            }
                            break;
                        case "J":{
                                int lowBytes = currentFrame.pop();
                                int objRef = currentFrame.pop();
                                long nvalue = (long)(value << 32 | lowBytes);
                                ClassLoader.heap.putLongField(objRef, fname, nvalue);
                            }
                            break;
                        case "D":{
                                int lowBytes = currentFrame.pop();
                                int objRef = currentFrame.pop();
                                long nvalue = (long)(value << 32 | lowBytes);
                                ClassLoader.heap.putLongField(objRef, fname, nvalue);
                            }
                            break;
                    }
                    break;
                }

                // Method invocation
                case 0xb6: { // invokevirtual
                    int methodIndex = fetchShort();

                    int name_and_type_index = ((ClassFileParser.CONSTANT_Methodref_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), methodIndex)).name_and_type_index;
                    ClassFileParser.CONSTANT_NameAndType_info name_and_type_info = (ClassFileParser.CONSTANT_NameAndType_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), name_and_type_index);
                    String methodName = getConstantUTF8(name_and_type_info.name_index);
                    String descriptor = getConstantUTF8(name_and_type_info.descriptor_index);

                    int argSlots = getArgumentCount(descriptor);
                    int[] args = new int[argSlots];
                    for (int i = argSlots - 1; i >= 0; i--) {
                        args[i] = currentFrame.pop();
                    }

                    int objRef = currentFrame.pop();
                    // if (objRef == 0) throw new RuntimeException("NullPointerException");

                    String objectClassName = ClassLoader.heap.getObjectClassName(objRef);
                    ClassFileParser.Code_attribute cattr = ClassLoader.heap.getMethodCode(objectClassName, methodName);

                    StackFrame newFrame = new StackFrame(cattr.max_locals, cattr.max_stack, objectClassName, methodName);
                    newFrame.setLocal(0, objRef);
                    for (int i = 0; i < argSlots; i++) {
                        newFrame.setLocal(1 + i, args[i]);
                    }

                    callStack.push(currentFrame);
                    currentFrame.setReturnPc(pc);
                    currentFrame = newFrame;
                    bytecode = cattr.code;
                    pc = 0;
                    break;
                }
                case 0xb7: { // invokespecial (constructor or private method)
                    int methodIndex = fetchShort();

                    int class_index = ((ClassFileParser.CONSTANT_Methodref_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), methodIndex)).class_index;
                    String className = getConstantUTF8(((ClassFileParser.CONSTANT_Class_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), class_index)).name_index);

                    if (className.equals("java/lang/Object")) {
                        currentFrame.pop(); // Pop the object reference
                    } else {
                        // Get method info
                        int name_and_type_index = ((ClassFileParser.CONSTANT_Methodref_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), methodIndex)).name_and_type_index;
                        ClassFileParser.CONSTANT_NameAndType_info name_and_type_info = (ClassFileParser.CONSTANT_NameAndType_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), name_and_type_index);
                        String methodName = getConstantUTF8(name_and_type_info.name_index);
                        String descriptor = getConstantUTF8(name_and_type_info.descriptor_index);

                        int argSlots = getArgumentCount(descriptor);
                        int[] args = new int[argSlots];
                        for (int i = argSlots - 1; i >= 0; i--) {
                            args[i] = currentFrame.pop();
                        }

                        // Save current state
                        callStack.push(currentFrame);
                        currentFrame.setReturnPc(pc);

                        // Get new method's code
                        ClassFileParser.Code_attribute cattr = ClassLoader.heap.getMethodCode(className, methodName);

                        // Create new frame
                        StackFrame newFrame = new StackFrame(cattr.max_locals, cattr.max_stack, className, methodName);

                        // For a constructor (<init>), the first argument is the uninitialized object reference.
                        // We assume no other arguments for simplicity here.
                        int objRef = currentFrame.pop();
                        newFrame.setLocal(0, objRef); // 'this' is local variable 0

                        for (int i = 0; i < argSlots; i++) {
                            newFrame.setLocal(1 + i, args[i]);
                        }

                        // Switch context
                        currentFrame = newFrame;
                        bytecode = cattr.code;
                        pc = 0;
                    }
                    break;
                }
                case 0xb8: { // invokestatic
                    int methodIndex = fetchShort();

                    int class_index = ((ClassFileParser.CONSTANT_Methodref_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), methodIndex)).class_index;
                    String className = getConstantUTF8(((ClassFileParser.CONSTANT_Class_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), class_index)).name_index);

                    int name_and_type_index = ((ClassFileParser.CONSTANT_Methodref_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), methodIndex)).name_and_type_index;
                    ClassFileParser.CONSTANT_NameAndType_info name_and_type_info = (ClassFileParser.CONSTANT_NameAndType_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), name_and_type_index);
                    String methodName = getConstantUTF8(name_and_type_info.name_index);
                    String descriptor = getConstantUTF8(name_and_type_info.descriptor_index);

                    int argSlots = getArgumentCount(descriptor);
                    int[] args = new int[argSlots];
                    for (int i = argSlots - 1; i >= 0; i--) {
                        args[i] = currentFrame.pop();
                    }

                    // Save current state
                    callStack.push(currentFrame);
                    currentFrame.setReturnPc(pc);

                    // Get new method's code
                    ClassFileParser.Code_attribute cattr = ClassLoader.heap.getMethodCode(className, methodName);

                    // Create new frame
                    StackFrame newFrame = new StackFrame(cattr.max_locals, cattr.max_stack, className, methodName);

                    // For static methods, arguments start at local 0
                    for (int i = 0; i < argSlots; i++) {
                        newFrame.setLocal(i, args[i]);
                    }

                    // Switch context
                    currentFrame = newFrame;
                    bytecode = cattr.code;
                    pc = 0;
                    break;
                }

                // Return
                case 0xac: { // ireturn
                    int retVal = currentFrame.pop();
                    if (callStack.isEmpty()) {
                        return retVal;
                    } else {
                        StackFrame previousFrame = callStack.pop();
                        pc = previousFrame.getReturnPc();
                        currentFrame = previousFrame;
                        ClassFileParser.Code_attribute cattr = ClassLoader.heap.getMethodCode(currentFrame.getClassName(), currentFrame.getMethodName()); // Assuming we return to main, needs generalization
                        bytecode = cattr.code;
                        currentFrame.push(retVal);
                    }
                    break;
                }
                case 0xb1: { // return (void)
                    if (callStack.isEmpty()) {
                        return 0;
                    } else {
                        StackFrame previousFrame = callStack.pop();
                        pc = previousFrame.getReturnPc();
                        currentFrame = previousFrame;
                        ClassFileParser.Code_attribute cattr = ClassLoader.heap.getMethodCode(currentFrame.getClassName(), currentFrame.getMethodName()); // Assuming we return to main, needs generalization
                        bytecode = cattr.code;
                    }
                    break;
                }

                default:
                    throw new RuntimeException("Unsupported opcode: 0x" + Integer.toHexString(opcode));
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

    private int getArgumentCount(String descriptor) {
        int count = 0;
        int i = 1; // Skip '('
        while (descriptor.charAt(i) != ')') {
            char c = descriptor.charAt(i);
            if (c == 'L') {
                count++;
                while (descriptor.charAt(i) != ';') i++;
                i++;
            } else if (c == '[') {
                while (descriptor.charAt(i) == '[') i++;
                if (descriptor.charAt(i) == 'L') {
                    while (descriptor.charAt(i) != ';') i++;
                }
                count++;
                i++;
            } else if (c == 'D' || c == 'J') {
                count += 2;
                i++;
            } else {
                count++;
                i++;
            }
        }
        return count;
    }

    private String getConstantUTF8(int index){
        return ((ClassFileParser.CONSTANT_Utf8_info)ClassLoader.heap.getConstantPoolEntry(currentFrame.getClassName(), index)).bytes;
    }
}


class StackFrame {
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
        if (sp < 0) throw new RuntimeException("Stack underflow");
        return operandStack[sp--];
    }


    public int getLocal(int index) {
        return locals[index];
    }


    public void setLocal(int index, int value) {
        locals[index] = value;
    }

    public void setReturnPc(int pc){
        this.returnPc = pc;
    }

    public int getReturnPc(){
        return this.returnPc;
    }

    public String getClassName(){
        return this.className;
    }

    public String getMethodName(){
        return this.methodName;
    }
}


