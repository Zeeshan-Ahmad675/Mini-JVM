import java.util.*;

public class Memory {
    protected static Heap heap = new Heap();
}


class Heap {
    private Map<Integer, ObjectInstance> objects = new HashMap<>();
    private Map<String, ClassFileParser.cp_info[]> constant_pool_area = new HashMap<>();
    private Map<String, ClassFileParser.method_info[]> method_area = new HashMap<>();
    private Map<String, ClassFileParser.field_info[]> field_area = new HashMap<>();
    private int nextId = 1;


    public int newObject(int fieldCount) {
        int id = nextId++;
        objects.put(id, new ObjectInstance(fieldCount));
        return id;
    }


    public void putField(int objId, int fieldIndex, int value) {
        ObjectInstance obj = objects.get(objId);
        if (obj == null) throw new RuntimeException("Null object reference");
        obj.setField(fieldIndex, value);
    }


    public int getField(int objId, int fieldIndex) {
        ObjectInstance obj = objects.get(objId);
        if (obj == null) throw new RuntimeException("Null object reference");
        return obj.getField(fieldIndex);
    }
}

class ObjectInstance {
    private int[] fields;


    public ObjectInstance(int fieldCount) {
        fields = new int[fieldCount];
    }


    public void setField(int index, int value) {
        if (index < 0 || index >= fields.length) throw new RuntimeException("Invalid field index");
        fields[index] = value;
    }


    public int getField(int index) {
        if (index < 0 || index >= fields.length) throw new RuntimeException("Invalid field index");
        return fields[index];
    }
}

