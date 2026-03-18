import java.io.*;
import java.util.*;

public class ClassLoader {
    protected static Heap heap = new Heap();

    /**
     * Constructs a new ClassLoader.
     * @param heap The JVM's heap, used to store loaded class data.
     * @param classPath The root directory to search for class files.
     */
    public ClassLoader(String classPath){
        // this.classPath = classPath ;
    }

      /**
     * Loads a class by its fully qualified name.
     * @param className The name of the class to load (e.g., "java.lang.Object", "com.example.MyClass").
     * @throws IOException if the class file cannot be found or read.
     */


    public static void loadClass(String className) throws IOException{
       // Step 1: Check if the class is already loaded to avoid redundant work.
       if(heap.isClassLoaded(className)){
            System.out.println("[ClassLoader] Info: Class " + className + " is already loaded.");
            return;
       }
        // Step 2: Convert the Java class name to a file system path.
        // e.g., "com.example.Test" -> "com\example\Test.class"
        String classFilePath = className.replace('.', File.separatorChar) + ".class";
        File classFile = new File(classFilePath);

        if(!classFile.exists()){
          throw new IOException("ClassNotFoundException: Could not find a class " + className + " at path " + classFile.getAbsolutePath()) ;
        }
        System.out.println("[ClassLoader] Loading class: " + className);


        try(FileInputStream fis = new FileInputStream(classFile)) {
            byte[] classData = fis.readAllBytes();
            ClassFile classFileStructure = ClassFileParser.parse(classData);
            heap.storeClass(
              className,
              classFileStructure.getConstantPool(),
              classFileStructure.getMethods(),
              classFileStructure.getFields()
            );
            System.out.println("[ClassLoader] Successfully loaded and stored class: " + className);
        }
        catch(IOException e){
            System.out.println("Error in loading file.");
            e.printStackTrace();
        }
        catch(Exception e){
            System.out.println("Error in loading file. Not an IO error " + e.getMessage());
            e.printStackTrace();
        }

     }
}


class Heap {
    private Map<Integer, ObjectInstance> objects = new HashMap<>();
    private Map<String, ClassFileParser.cp_info[]> constant_pool_area = new HashMap<>();
    private Map<String, ClassFileParser.method_info[]> method_area = new HashMap<>();
    private Map<String, ClassFileParser.field_info[]> field_area = new HashMap<>();
    private Map<String, Map<String, Object>> static_area = new HashMap<>();
    private Map<String, Boolean> classInitialized = new HashMap<>();
    private Map<String, Integer> string_constant_pool = new HashMap<>();
    private int nextId = 1;

    public boolean isClassLoaded(String className){
        return method_area.containsKey(className);
    }
    public boolean isClassInitialized(String className){
        return classInitialized.containsKey(className);
    }

    public ClassFileParser.cp_info getConstantPoolEntry(String className, int index){
        return this.constant_pool_area.get(className)[index];
    }
    
    public void putStaticField(String className, String fieldName, Object value){
        if(static_area.get(className) == null) static_area.put(className, new HashMap<>());
        static_area.get(className).put(fieldName, value);
    }

    public Object getStaticField(String className, String fieldName) throws IOException {
        if(!isClassLoaded(className)) ClassLoader.loadClass(className);
        return static_area.get(className).get(fieldName);
    }

    public ClassFileParser.Code_attribute getMethodCode(String className, String methodName) throws IOException {
        if(!isClassLoaded(className)) ClassLoader.loadClass(className);
        for(ClassFileParser.method_info method : method_area.get(className)){
            String current_method = ((ClassFileParser.CONSTANT_Utf8_info)getConstantPoolEntry(className, method.name_index)).bytes;
            if(current_method.equals(methodName)){
                for(ClassFileParser.attribute_info attr : method.attributes){
                    String current_attr = ((ClassFileParser.CONSTANT_Utf8_info)getConstantPoolEntry(className, attr.attribute_name_index)).bytes;
                    if(current_attr.equals("Code")){
                        return (ClassFileParser.Code_attribute)attr;
                    }
                }
            }
        }
        return null;
    }


    public void storeClass(String className, ClassFileParser.cp_info[] cp, ClassFileParser.method_info[] methods, ClassFileParser.field_info[] fields){
        constant_pool_area.put(className,cp) ;
        method_area.put(className,methods);
        field_area.put(className,fields);
        for(ClassFileParser.field_info field : fields){
            if((field.access_flags & 0x0008) == 0) continue;
            Object value = 0;
            ClassFileParser.CONSTANT_Utf8_info field_name = (ClassFileParser.CONSTANT_Utf8_info)(ClassLoader.heap.getConstantPoolEntry(className, field.name_index));
            for(ClassFileParser.attribute_info attr :  field.attributes){
                if(((ClassFileParser.CONSTANT_Utf8_info)(ClassLoader.heap.getConstantPoolEntry(className, attr.attribute_name_index))).bytes.equals("ConstantValue")){
                    ClassFileParser.cp_info avalue = ClassLoader.heap.getConstantPoolEntry(className, ((ClassFileParser.ConstantValue_attribute)attr).constantvalue_index);
                    switch(avalue.tag){
                        case 3:
                            value = ((ClassFileParser.CONSTANT_Integer_info)avalue).bytes;
                            break;
                        case 4:
                            value = ((ClassFileParser.CONSTANT_Float_info)avalue).bytes;
                            break;
                        case 5:
                            value = (long)((ClassFileParser.CONSTANT_Long_info)avalue).high_bytes << 32 | (((ClassFileParser.CONSTANT_Long_info)avalue).low_bytes & 0xFFFFFFFFL);
                            break;
                        case 6:
                            value = ((ClassFileParser.CONSTANT_Double_info)avalue).high_bytes << 32 | (((ClassFileParser.CONSTANT_Double_info)avalue).low_bytes & 0xFFFFFFFFL);
                            break;
                        default:
                            break;
                    }
                if(static_area.get(className) == null) static_area.put(className, new HashMap<>());
                static_area.get(className).put(field_name.bytes, value);
                break;
                }
            }
        }
    }

    public int newObject(String className) throws IOException{
        if(!constant_pool_area.containsKey(className)) {
            ClassLoader.loadClass(className);
        }
        int id = nextId++;
        List<ClassFileParser.field_info> fields = new ArrayList<>(Arrays.asList(field_area.get(className))) ;
        if(fields != null){
            // ACC_STATIC flag is 0x0008. We only allocate space for non-static fields.
            fields.removeIf(field -> (field.access_flags & 0x0008) != 0);
        }
        objects.put(id, new ObjectInstance(className, fields)) ;
        // if(className.equals("java/lang/String")) string_constant_pool.put(getStringField(id), id);
        return id;
    }

    public void putIntField(int objId, String fieldName, int value) {
        ObjectInstance obj = objects.get(objId);
        if (obj == null) throw new RuntimeException("Null object reference");
        obj.setField(fieldName, value);
    }

    public void putLongField(int objId, String fieldName, long value) {
        ObjectInstance obj = objects.get(objId);
        if (obj == null) throw new RuntimeException("Null object reference");
        obj.setField(fieldName, value);
    }

    public void putStringField(int objId, String fieldName, String value){
        ObjectInstance obj = objects.get(objId);
        if (obj == null) throw new RuntimeException("Null object reference");
        obj.setField(fieldName, value);
    }


    public int getIntField(int objId, String fieldName) {
        ObjectInstance obj = objects.get(objId);
        if (obj == null) throw new RuntimeException("Null object reference");
        return (int)obj.getField(fieldName);
    }

    public long getLongField(int objId, String fieldName) {
        ObjectInstance obj = objects.get(objId);
        if (obj == null) throw new RuntimeException("Null object reference");
        return (long)obj.getField(fieldName);
    }

    public String getStringField(int objId){
        ObjectInstance obj = objects.get(objId);
        if(obj == null || obj.getClassName() != "java/lang/String") return null;
        else return (String)obj.getField("value");
    }

    public int getStringObjectReference(String literal) throws IOException{
        Object ref = string_constant_pool.get(literal);
        if(ref == null) {
            int idx = newObject("java/lang/String");
            putStringField(idx, "value", literal);
            string_constant_pool.put(getStringField(idx), idx);
            return idx;
        }
        return (int)ref;
    }

    public String getObjectClassName(int objId) {
        ObjectInstance obj = objects.get(objId);
        if (obj == null) throw new RuntimeException("Null object reference");
        return obj.getClassName();
    }

    public void setClassInitialized(String className){
        classInitialized.put(className, true);
    }
}

class ObjectInstance {
    private final String className;
    private Map<String, Object> fields = new HashMap<>();


    public ObjectInstance(String className, List<ClassFileParser.field_info> fields) {
        this.className = className ;
        for(ClassFileParser.field_info field : fields){
            Object value = 0;
            ClassFileParser.CONSTANT_Utf8_info field_name = (ClassFileParser.CONSTANT_Utf8_info)(ClassLoader.heap.getConstantPoolEntry(className, field.name_index));
            this.fields.put(field_name.bytes, value);
        }
    }

    public String getClassName(){
        return className ;
    }


    public void setField(String fieldName, Object value) {
        if (!fields.containsKey(fieldName)) throw new RuntimeException("Invalid field name " + fieldName + " of class " + className);
        fields.put(fieldName, value);
    }


    public Object getField(String fieldName) {
        if (!fields.containsKey(fieldName)) throw new RuntimeException("The field does not exist");
        return fields.get(fieldName);
    }
}

