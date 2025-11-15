import java.io.*;

public class ClassFileParser {
    static abstract class cp_info {
        protected int tag;
    }
    static class CONSTANT_Class_info extends cp_info {
        int name_index;

        public CONSTANT_Class_info(int name_index) {
            this.tag = 7;
            this.name_index = name_index;
        }
    }
    static class CONSTANT_Fieldref_info extends cp_info {
        int class_index;
        int name_and_type_index;

        public CONSTANT_Fieldref_info(int class_index, int name_and_type_index) {
            this.tag = 9;
            this.class_index = class_index;
            this.name_and_type_index = name_and_type_index;
        }
    }
    static class CONSTANT_Methodref_info extends cp_info {
        int class_index;
        int name_and_type_index;

        public CONSTANT_Methodref_info(int class_index, int name_and_type_index) {
            this.tag = 10;
            this.class_index = class_index;
            this.name_and_type_index = name_and_type_index;
        }
    }
    static class CONSTANT_InterfaceMethodref_info extends cp_info {
        int class_index;
        int name_and_type_index;

        public CONSTANT_InterfaceMethodref_info(int class_index, int name_and_type_index) {
            this.tag = 11;
            this.class_index = class_index;
            this.name_and_type_index = name_and_type_index;
        }
    }
    static class CONSTANT_String_info extends cp_info {
        int string_index;

        public CONSTANT_String_info(int string_index) {
            this.tag = 8;
            this.string_index = string_index;
        }
    }
    static class CONSTANT_Integer_info extends cp_info {
        int bytes;

        public CONSTANT_Integer_info(int bytes) {
            this.tag = 3;
            this.bytes = bytes;
        }
    }
    static class CONSTANT_Float_info extends cp_info {
        int bytes;

        public CONSTANT_Float_info(int bytes) {
            this.tag = 4;
            this.bytes = bytes;
        }
    }
    static class CONSTANT_Long_info extends cp_info {
        int high_bytes;
        int low_bytes;

        public CONSTANT_Long_info(int high_bytes, int low_bytes) {
            this.tag = 5;
            this.high_bytes = high_bytes;
            this.low_bytes = low_bytes;
        }
    }
    static class CONSTANT_Double_info extends cp_info {
        int high_bytes;
        int low_bytes;

        public CONSTANT_Double_info(int high_bytes, int low_bytes) {
            this.tag = 6;
            this.high_bytes = high_bytes;
            this.low_bytes = low_bytes;
        }
    }
    static class CONSTANT_NameAndType_info extends cp_info {
        int name_index;
        int descriptor_index;

        public CONSTANT_NameAndType_info(int name_index, int descriptor_index) {
            this.tag = 12;
            this.name_index = name_index;
            this.descriptor_index = descriptor_index;
        }
    }
    static class CONSTANT_Utf8_info extends cp_info {
        int length;
        String bytes;

        public CONSTANT_Utf8_info(int length, String bytes) {
            this.tag = 1;
            this.length = length;
            this.bytes = bytes;
        }
    }
    static class CONSTANT_MethodHandle_info extends cp_info{
        int reference_kind;
        int reference_index;

        public CONSTANT_MethodHandle_info(int reference_kind, int reference_index){
            this.tag = 15;
            this.reference_kind = reference_kind;
            this.reference_index = reference_index;
        }
    }
    static class CONSTANT_MethodType_info extends cp_info{
        int descriptor_index;

        public CONSTANT_MethodType_info(int decriptor_index){
            this.tag = 16;
            this.descriptor_index = decriptor_index;
        }
    }
    static class CONSTANT_InvokeDynamic_info extends cp_info {
        int bootstrap_method_attr_index;
        int name_and_type_index;

        public CONSTANT_InvokeDynamic_info(int bootstrap_method_attr_index, int name_and_type_index){
            this.tag = 18;
            this.bootstrap_method_attr_index = bootstrap_method_attr_index;
            this.name_and_type_index = name_and_type_index;
        }
    }


    static abstract class attribute_info {
        int attribute_name_index;
        int attribute_length;
    }
    static class ConstantValue_attribute extends attribute_info {
        int constantvalue_index;

        public ConstantValue_attribute(int attribute_name_index, int attribute_length, int constantvalue_index) {
            this.attribute_name_index = attribute_name_index;
            this.attribute_length = attribute_length;
            this.constantvalue_index = constantvalue_index;
        }
    }

    static class Code_attribute extends attribute_info {
        int max_stack;
        int max_locals;
        int code_length;
        byte[] code;
        int exception_table_length;
        exception_info[] exception_table;
        int attributes_count;
        attribute_info[] attributes;

        public Code_attribute(int attribute_name_index, int attribute_length, int max_stack, int max_locals, int code_length, byte[] code, int exception_table_length, exception_info[] exception_table, int attributes_count, attribute_info[] attributes) {
            this.attribute_name_index = attribute_name_index;
            this.attribute_length = attribute_length;
            this.max_stack = max_stack;
            this.max_locals = max_locals;
            this.code_length = code_length;
            this.code = code;
            this.exception_table_length = exception_table_length;
            this.exception_table = exception_table;
            this.attributes_count = attributes_count;
            this.attributes = attributes;
        }
    }

    static class exception_info {
        int start_pc;
        int end_pc;
        int handler_pc;
        int catch_type;

        public exception_info(int start_pc, int end_pc, int handler_pc, int catch_type) {
            this.start_pc = start_pc;
            this.end_pc = end_pc;
            this.handler_pc = handler_pc;
            this.catch_type = catch_type;
        }
    }

    static class Exceptions_attribute extends attribute_info {
        int number_of_exceptions;
        int[] exception_index_table;

        public Exceptions_attribute(int attribute_name_index, int attribute_length, int number_of_exceptions, int[] exception_index_table) {
            this.attribute_name_index = attribute_name_index;
            this.attribute_length = attribute_length;
            this.number_of_exceptions = number_of_exceptions;
            this.exception_index_table = exception_index_table;
        }
    }

    static class Synthetic_attribute extends attribute_info {
        public Synthetic_attribute(int attribute_name_index, int attribute_length) {
            this.attribute_name_index = attribute_name_index;
            this.attribute_length = attribute_length;
        }
    }

    static class Signature_attribute extends attribute_info {
        int signature_index;
        public Signature_attribute(int attribute_name_index, int attribute_length, int signature_index) {
            this.attribute_name_index = attribute_name_index;
            this.attribute_length = attribute_length;
            this.signature_index = signature_index;
        }
    }

    // static class LineNumberTable_attribute extends attribute_info {
    //     int line_number_table_length;
    //     line_number_info[] line_number_table;

    //     public LineNumberTable_attribute(int attribute_name_index, int attribute_length, int line_number_table_length, line_number_info[] line_number_table) {
    //         this.attribute_name_index = attribute_name_index;
    //         this.attribute_length = attribute_length;
    //         this.line_number_table_length = line_number_table_length;
    //         this.line_number_table = line_number_table;
    //     }
    // }

    // static class line_number_info {
    //     int start_pc;
    //     int line_number;

    //     public line_number_info(int start_pc, int line_number) {
    //         this.start_pc = start_pc;
    //         this.line_number = line_number;
    //     }
    // }

    // static class SourceFile_attribute extends attribute_info {
    //     int sourcefile_index;

    //     public SourceFile_attribute(int attribute_name_index, int attribute_length, int sourcefile_index) {
    //         this.attribute_name_index = attribute_name_index;
    //         this.attribute_length = attribute_length;
    //         this.sourcefile_index = sourcefile_index;
    //     }
    // }

    // static class Deprecated_attribute extends attribute_info {
    //      public Deprecated_attribute(int attribute_name_index, int attribute_length) {
    //         this.attribute_name_index = attribute_name_index;
    //         this.attribute_length = attribute_length;
    //     }
    // }

    static class field_info {
        int access_flags;
        int name_index;
        int descriptor_index;
        int attributes_count;
        attribute_info[] attributes;

        public field_info (int access_flags, int name_index, int descriptor_index, int attributes_count, attribute_info[] attributes){
            this.access_flags = access_flags;
            this.name_index = name_index;
            this.descriptor_index = descriptor_index;
            this.attributes_count = attributes_count;
            this.attributes = attributes;
        }
    }
    static class method_info {
        int access_flags;
        int name_index;
        int descriptor_index;
        int attributes_count;
        attribute_info[] attributes;

        public method_info (int access_flags, int name_index, int descriptor_index, int attributes_count, attribute_info[] attributes){
            this.access_flags = access_flags;
            this.name_index = name_index;
            this.descriptor_index = descriptor_index;
            this.attributes_count = attributes_count;
            this.attributes = attributes;
        }
    }


    public static byte[] extractMethodBytecode(byte[] classFileBytes, String methodName) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(classFileBytes));
        // 1. Checks magic number
        int magic = in.readInt();
        if (magic != 0xCAFEBABE) throw new IOException("Invalid class file");

        // 2. Minor, major version
        int minor_version = in.readUnsignedShort();
        int major_version = in.readUnsignedShort();
        System.out.println("Version: " + major_version + "." + minor_version);

        // 3. Read constant pool count
        int constant_pool_count = in.readUnsignedShort();
        cp_info[] constant_pool = new cp_info[constant_pool_count];


        // Read constant pool entries
        for (int i = 1; i < constant_pool_count; i++) {
            int tag = in.readUnsignedByte();
            switch (tag) {
                case 7: // Class
                    {
                        int name_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_Class_info(name_index);
                        break;
                    }
                case 9:
                    {
                        int class_index = in.readUnsignedShort();
                        int name_and_type_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_Fieldref_info(class_index, name_and_type_index);
                        break;
                    }
                case 10:
                    {
                        int class_index = in.readUnsignedShort();
                        int name_and_type_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_Methodref_info(class_index, name_and_type_index);
                        break;
                    }
                case 11:
                    {
                        int class_index = in.readUnsignedShort();
                        int name_and_type_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_InterfaceMethodref_info(class_index, name_and_type_index);
                        break;
                    }
                case 8: // String
                    {
                        int string_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_String_info(string_index);
                        break;
                    }
                case 3:
                    {
                        int bytes = in.readInt();
                        constant_pool[i] = new CONSTANT_Integer_info(bytes);
                        break;
                    }
                case 4:
                    {
                        int bytes = in.readInt();
                        constant_pool[i] = new CONSTANT_Float_info(bytes);
                        break;
                    }
                case 5:
                    {
                        int high_bytes = in.readInt();
                        int low_bytes = in.readInt();
                        constant_pool[i] = new CONSTANT_Long_info(high_bytes, low_bytes);
                        break;
                    }
                case 6:
                    {
                        int high_bytes = in.readInt();
                        int low_bytes = in.readInt();
                        constant_pool[i] = new CONSTANT_Double_info(high_bytes, low_bytes);
                        break;
                    }
                case 12:
                    {
                        int name_index = in.readUnsignedShort();
                        int decriptor_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_NameAndType_info(name_index, decriptor_index);
                        break;
                    }
                case 1: // Utf8
                    {
                        int length = in.readUnsignedShort();
                        byte[] utf = new byte[length];
                        in.readFully(utf);
                        constant_pool[i] = new CONSTANT_Utf8_info(length, new String(utf, "UTF-8"));
                        break;
                    }
                case 15:
                    {
                        int reference_kind = in.readUnsignedByte();
                        int reference_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_MethodHandle_info(reference_kind, reference_index);
                        break;
                    }
                case 16:
                    {
                        int decriptor_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_MethodType_info(decriptor_index);
                        break;
                    }
                case 18:
                    {
                        int bootstrap_method_attr_index = in.readUnsignedShort();
                        int name_and_type_index = in.readUnsignedShort();
                        constant_pool[i] = new CONSTANT_InvokeDynamic_info(bootstrap_method_attr_index, name_and_type_index);
                        break;
                    }
                default:
                    throw new IOException("Unknown constant pool tag: " + tag);
            }
        }

        // 4. Access flags, this class, super class
        int access_flags = in.readUnsignedShort();
        int this_class = in.readUnsignedShort();
        int super_class = in.readUnsignedShort();

        System.out.println("Access Flags: " + String.format("0x%04X", access_flags));
        System.out.println("Class: " + ((CONSTANT_Utf8_info)constant_pool[((CONSTANT_Class_info)constant_pool[this_class]).name_index]).bytes);
        System.out.println("Super Class: " + ((CONSTANT_Utf8_info)constant_pool[((CONSTANT_Class_info)constant_pool[super_class]).name_index]).bytes);


        // 5. Interfaces
        int interfaces_count = in.readUnsignedShort();
        int[] interfaces = new int[interfaces_count];
        for (int i = 0; i < interfaces_count; i++) interfaces[i] = in.readUnsignedShort();  

        // 6. Fields
        int fields_count = in.readUnsignedShort();
        System.out.println("Fields count: " + fields_count);
        field_info[] fields = new field_info[fields_count];
        for (int i = 0; i < fields_count; i++) {
            int f_access_flags = in.readUnsignedShort();
            int name_index = in.readUnsignedShort();
            int descriptor_index = in.readUnsignedShort();

            System.out.println("Field name: " + ((CONSTANT_Utf8_info)constant_pool[name_index]).bytes);
            System.out.println("Field Description: " + ((CONSTANT_Utf8_info)constant_pool[descriptor_index]).bytes);

            int attributes_count = in.readUnsignedShort();
            fields[i] = new field_info(f_access_flags, name_index, descriptor_index, attributes_count, new attribute_info[attributes_count]);

            for (int j = 0; j < attributes_count; j++) {
                int attribute_name_index = in.readUnsignedShort();
                int attribute_length = in.readInt();
                switch(((CONSTANT_Utf8_info)constant_pool[attribute_name_index]).bytes){
                    case "ConstantValue":
                        {   
                            int constantvalue_index = in.readUnsignedShort();
                            fields[i].attributes[j] = new ConstantValue_attribute(attribute_name_index, attribute_length, constantvalue_index);
                            break;
                        }
                    case "Synthetic":
                        {   
                            fields[i].attributes[j] = new Synthetic_attribute(attribute_name_index, attribute_length);
                            break;
                        }
                    case "Signature":
                        {   
                            int signature_index = in.readUnsignedShort();
                            fields[i].attributes[j] = new Signature_attribute(attribute_name_index, attribute_length, signature_index);
                            break;
                        }
                    // Needs annotation support. So, @Override among other annotations are not supported yet.
                    // Further, have no stack frame map, so we are assuming the class files to be inherently safe for now !!
                    default:
                        in.skipBytes(attribute_length);
                }
            }
        }


        // 7. Read methods and find target method
        int methods_count = in.readUnsignedShort();
        method_info[] methods = new method_info[methods_count];
        for (int i = 0; i < methods_count; i++) {
            int m_access_flags = in.readUnsignedShort();
            int name_index = in.readUnsignedShort();
            int descriptor_index = in.readUnsignedShort();
            System.out.println("Method name: " + ((CONSTANT_Utf8_info)constant_pool[name_index]).bytes);
            System.out.println("Method Description: " + ((CONSTANT_Utf8_info)constant_pool[descriptor_index]).bytes);
            String current_method_name = ((CONSTANT_Utf8_info)constant_pool[name_index]).bytes;
            int attributes_count = in.readUnsignedShort();
            methods[i] = new method_info(m_access_flags, name_index, descriptor_index, attributes_count, new attribute_info[attributes_count]);

            for (int j = 0; j < attributes_count; j++) {
                int attribute_name_index = in.readUnsignedShort();
                int attribute_length = in.readInt();
                String attribute_name = ((CONSTANT_Utf8_info)constant_pool[attribute_name_index]).bytes;
                switch(attribute_name){
                    case "Code":
                        {
                            int max_stack = in.readUnsignedShort();
                            int max_locals = in.readUnsignedShort();
                            int code_length = in.readInt();
                            byte[] code = new byte[code_length];
                            in.readFully(code);

                            // Skip exception table
                            int exception_table_length = in.readUnsignedShort();
                            for (int k = 0; k < exception_table_length; k++) {
                                in.readUnsignedShort(); in.readUnsignedShort(); in.readUnsignedShort(); in.readUnsignedShort();
                            }
                            // Skip code attributes
                            int code_attribute_count = in.readUnsignedShort();
                            for (int k = 0; k < code_attribute_count; k++) {
                                in.readUnsignedShort();
                                int len = in.readInt();
                                in.skipBytes(len);
                            }
                            methods[i].attributes[j] = new Code_attribute(attribute_name_index, attribute_length, max_stack, max_locals, code_length, code, exception_table_length, null, code_attribute_count, null);
                            break;
                        }
                    case "Exceptions":
                        {
                            int number_of_exceptions = in.readUnsignedShort();
                            int[] exception_index_table = new int[number_of_exceptions];
                            for(int k = 0 ; k < number_of_exceptions; k++){
                                exception_index_table[k] = in.readUnsignedShort();
                            }
                            methods[i].attributes[j] = new Exceptions_attribute(attribute_name_index, attribute_length, number_of_exceptions, exception_index_table);
                            break;
                        }
                    default:
                }
            }
            
            if (current_method_name.equals(methodName)) {
                for (attribute_info attr : methods[i].attributes) {
                    if (attr instanceof Code_attribute) {
                        return ((Code_attribute) attr).code;
                    }
                }
            }
        }

        throw new IOException("Method " + methodName + " not found or has no code attribute");
    }

    public static byte[] main(String[] args) throws IOException {
        File classFile = new File(args[0] + ".class");
        FileInputStream fis = new FileInputStream(classFile);
        byte[] classData = fis.readAllBytes();
        fis.close();

        byte[] mainBytecode = extractMethodBytecode(classData, args[1]);

        // System.out.println("Main method bytecode (" + mainBytecode.length + " bytes):");
        // for (byte b : mainBytecode) {
        //     System.out.println(String.format("0x%02X", b));
        // }

        return mainBytecode;
    }
}
