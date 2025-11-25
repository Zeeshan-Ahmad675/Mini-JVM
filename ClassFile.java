public class ClassFile {
    private final String className;
    private final ClassFileParser.cp_info[] constantPool;
    private final ClassFileParser.field_info[] fields;
    private final ClassFileParser.method_info[] methods;

    public ClassFile(String className, ClassFileParser.cp_info[] constantPool, ClassFileParser.field_info[] fields, ClassFileParser.method_info[] methods) {
        this.className = className;
        this.constantPool = constantPool;
        this.fields = fields;
        this.methods = methods;
    }

    public String getClassName() {
        return className;
    }

    public ClassFileParser.cp_info[] getConstantPool() {
        return constantPool;
    }

    public ClassFileParser.field_info[] getFields() {
        return fields;
    }

    public ClassFileParser.method_info[] getMethods() {
        return methods;
    }
}
