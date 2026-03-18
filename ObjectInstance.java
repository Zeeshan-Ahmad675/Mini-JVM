import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ObjectInstance {
    private final String className;
    private Map<String, Object> fields = new HashMap<>();

    public ObjectInstance(String className, List<ClassFileParser.field_info> fields) {
        this.className = className;
        for (ClassFileParser.field_info field : fields) {
            Object value = 0;
            ClassFileParser.CONSTANT_Utf8_info field_name = (ClassFileParser.CONSTANT_Utf8_info) (ClassLoader.heap
                    .getConstantPoolEntry(className, field.name_index));
            this.fields.put(field_name.bytes, value);
        }
    }

    public String getClassName() {
        return className;
    }

    public void setField(String fieldName, Object value) {
        if (!fields.containsKey(fieldName))
            throw new RuntimeException("Invalid field name " + fieldName + " of class " + className);
        fields.put(fieldName, value);
    }

    public Object getField(String fieldName) {
        if (!fields.containsKey(fieldName))
            throw new RuntimeException("The field does not exist");
        return fields.get(fieldName);
    }
}
