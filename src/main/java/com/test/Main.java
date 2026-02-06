package com.test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;

public class Main {
    public static void main(String[] args) {
        Class<?> clazz = Test.class;
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(ManageUrl.class)) {
                ManageUrl ann = m.getAnnotation(ManageUrl.class);
                System.out.println(ann.value());
            }
        }

        File rootDir = new File("D:\\\\S5\\\\Framework\\\\Fw2\\\\test\\\\src\\\\main\\\\java\\\\com");
        Class<? extends Annotation> annotation = AnnotationController.class;
        List<Class<?>> annotated = findAnnotatedClasses(rootDir, "com", annotation);
        for (Class<?> cls : annotated) {
            System.out.println("Found annotated class: " + cls.getName());
        }
    }

   private static List<Class<?>> findAnnotatedClasses(File directory, String packageName, Class<? extends Annotation> annotation) {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively search sub-packages
                classes.addAll(findAnnotatedClasses(file, packageName + "." + file.getName(), annotation));
            } else if (file.getName().endsWith(".java") || file.getName().endsWith(".class")) {
                String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
                try {
                    Class<?> cls = Class.forName(packageName + "." + className);
                    if (cls.isAnnotationPresent(annotation)) {
                        classes.add(cls);
                    }
                } catch (Throwable e) {
                    System.out.println("[DEBUG] Could not load class: " + packageName + "." + className + " -> " + e);
                }
            }
        }

        return classes;
    }

}