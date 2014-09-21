package org.mitallast;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleClassTransformer implements ClassFileTransformer {

    private final Set<String> excludes;
    private final List<String> excludeMasks;

    public SimpleClassTransformer() {
        excludes = getExcludes();
        excludeMasks = getExcludeMasks();
    }

    public byte[] transform(ClassLoader loader,
                            String className,
                            Class classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] byteCode)
        throws IllegalClassFormatException {

        String normalizedClassName = className.replaceAll("/", ".");
        if (!isExclude(normalizedClassName)) {
            try {
                byteCode = transformClass(normalizedClassName, byteCode);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("fail [" + normalizedClassName + "]");
            }
        }

        return byteCode;
    }

    private byte[] transformClass(String className, byte[] byteCode) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get(className);
        if (ctClass.isAnnotation()) {
            return byteCode;
        } else if (ctClass.isInterface()) {
            return byteCode;
        }
        try {
            int classId = SimpleProfiler.instance.registerClass(ctClass);
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod ctMethod : methods) {
                if ((ctMethod.getModifiers() & Modifier.ABSTRACT) > 0) {
                    continue;
                } else if ((ctMethod.getModifiers() & Modifier.NATIVE) > 0) {
                    continue;
                }
                int methodId = SimpleProfiler.instance.registerMethod(ctMethod);
                ctMethod.insertBefore("org.mitallast.SimpleProfiler.instance.push(" + classId + "," + methodId + ", System.nanoTime());");
                ctMethod.insertAfter("org.mitallast.SimpleProfiler.instance.pop(" + classId + "," + methodId + ", System.nanoTime());");
                ctMethod.getMethodInfo().rebuildStackMap(classPool);
            }
            return ctClass.toBytecode();
        } finally {
            ctClass.detach();
        }
    }

    private boolean isExclude(String className) {
        if (excludes.contains(className)) {
            return true;
        }
        for (String excludeMask : excludeMasks) {
            if (className.startsWith(excludeMask)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getExcludes() {
        Set<String> excludes = new HashSet<>();
        InputStream stream = null;
        BufferedReader reader = null;
        try {
            stream = SimpleClassTransformer.class.getResourceAsStream("/org/mitallast/excludes.txt");
            reader = new BufferedReader(new InputStreamReader(stream));
            String className;
            while ((className = reader.readLine()) != null) {
                excludes.add(className);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return excludes;
    }

    private List<String> getExcludeMasks() {
        List<String> excludes = new ArrayList<>();
        InputStream stream = null;
        BufferedReader reader = null;
        try {
            stream = SimpleClassTransformer.class.getResourceAsStream("/org/mitallast/exclude_masks.txt");
            reader = new BufferedReader(new InputStreamReader(stream));
            String className;
            while ((className = reader.readLine()) != null) {
                excludes.add(className);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return excludes;
    }
}
