import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

public class ClassLoader {
    protected static Heap heap = new Heap();

    public ClassLoader(String classPath) {
        // this.classPath = classPath ;
    }

    public static void loadClass(String className) throws IOException {
        // Step 1: Check if the class is already loaded to avoid redundant work.
        if (heap.isClassLoaded(className)) {
            System.out.println("[ClassLoader] Info: Class " + className + " is already loaded.");
            return;
        }
        // Step 2: Convert the Java class name to a file system path.
        // e.g., "com.example.Test" -> "com\example\Test.class"
        String classFilePath = className.replace('.', File.separatorChar) + ".class";
        File classFile = new File(classFilePath);

        if (!classFile.exists()) {
            throw new IOException("ClassNotFoundException: Could not find a class " + className + " at path "
                    + classFile.getAbsolutePath());
        }
        System.out.println("[ClassLoader] Loading class: " + className);

        try (FileInputStream fis = new FileInputStream(classFile)) {
            byte[] classData = fis.readAllBytes();
            ClassFile classFileStructure = ClassFileParser.parse(classData);
            heap.storeClass(
                    className,
                    classFileStructure.getConstantPool(),
                    classFileStructure.getMethods(),
                    classFileStructure.getFields());
            System.out.println("[ClassLoader] Successfully loaded and stored class: " + className);
        } catch (IOException e) {
            System.out.println("Error in loading file.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error in loading file. Not an IO error " + e.getMessage());
            e.printStackTrace();
        }

    }
}
