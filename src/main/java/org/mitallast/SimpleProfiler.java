package org.mitallast;

import javassist.CtClass;
import javassist.CtMethod;

import java.util.Timer;
import java.util.TimerTask;

public final class SimpleProfiler {
    public static final SimpleProfiler instance = new SimpleProfiler();

    private static final int bulkSize = 1000;

    private volatile int classListCount = -1;
    private volatile int methodListCount = -1;
    private volatile CtClass[] classList = new CtClass[bulkSize];
    private volatile CtMethod[] methodList = new CtMethod[bulkSize];

    private final SimpleProfile classInvocation = new SimpleProfile();
    private final SimpleProfile methodInvocation = new SimpleProfile();
    private final SimpleProfile classTime = new SimpleProfile();
    private final SimpleProfile methodTime = new SimpleProfile();

    private final SimpleThreadStack threadStack = new SimpleThreadStack();

    private SimpleProfiler() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int topSize = 10;

                int[] topMethodInvocation = methodInvocation.top(topSize);
                for (int i = 0; i < topMethodInvocation.length; i++) {
                    int methodId = topMethodInvocation[i];
                    System.out.println(" - top invocation [" + i
                        + "] method [" + methodList[methodId].getDeclaringClass().getName() + "#" + methodList[methodId].getName()
                        + "] invocations [" + methodInvocation.get(methodId) + "]");
                }

                int[] topMethodTime = methodTime.top(topSize);
                for (int i = 0; i < topMethodTime.length; i++) {
                    int methodId = topMethodTime[i];
                    System.out.println(" - top time [" + i
                        + "] method [" + methodList[methodId].getDeclaringClass().getName() + "#" + methodList[methodId].getName()
                        + "] time in us [" + methodTime.get(methodId) / 1000 + "]");
                }

                int[] topClassInvocation = classInvocation.top(topSize);
                for (int i = 0; i < topClassInvocation.length; i++) {
                    int classId = topClassInvocation[i];
                    System.out.println(" - top invocation  [" + i
                        + "] class [" + classList[classId].getName()
                        + "] invocations [" + classInvocation.get(classId) + "]");
                }

                int[] topClassTime = classTime.top(topSize);
                for (int i = 0; i < topClassTime.length; i++) {
                    int classId = topClassTime[i];
                    System.out.println(" - top time  [" + i
                        + "] class [" + classList[classId].getName()
                        + "] time in us [" + classTime.get(classId) / 1000 + "]");
                }
            }
        }, 10000, 10000);
    }

    public synchronized int registerClass(CtClass ctClass) {
        classListCount++;
        if (classListCount >= classList.length) {
            // resize class list
            CtClass[] newClassList = new CtClass[classList.length + 1000];
            System.arraycopy(classList, 0, newClassList, 0, classList.length);
            classList = newClassList;
        }
        classInvocation.resize(classListCount);
        classTime.resize(classListCount);
        classList[classListCount] = ctClass;
        return classListCount;
    }

    public synchronized int registerMethod(CtMethod ctMethod) {
        methodListCount++;
        if (methodListCount >= methodList.length) {
            // resize method list
            CtMethod[] newMethodList = new CtMethod[methodList.length + 1000];
            System.arraycopy(methodList, 0, newMethodList, 0, methodList.length);
            methodList = newMethodList;
        }
        methodInvocation.resize(methodListCount);
        methodTime.resize(methodListCount);
        methodList[methodListCount] = ctMethod;
        return methodListCount;
    }

    public void push(int classId, int methodId, long startTime) {
        classInvocation.increment(classId);
        methodInvocation.increment(methodId);
        threadStack.push(classId, methodId, startTime);
    }

    public void pop(int classId, int methodId, long endTime) {
        long time = threadStack.pop(classId, methodId, endTime);
        classTime.increase(classId, time);
        methodTime.increase(methodId, time);
    }
}
