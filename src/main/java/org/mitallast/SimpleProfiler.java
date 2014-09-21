package org.mitallast;

import javassist.CtClass;
import javassist.CtMethod;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

public final class SimpleProfiler {
    public static final SimpleProfiler instance = new SimpleProfiler();

    private static final int bulkSize = 1000;

    private volatile int classListCount=-1;
    private volatile int methodListCount=-1;
    private volatile CtClass[] classList = new CtClass[bulkSize];
    private volatile CtMethod[] methodList = new CtMethod[bulkSize];

    private volatile long[] invocationClassCount=new long[1000];
    private volatile long[] invocationMethodCount=new long[1000];

    private static final Unsafe unsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);

            unsafe = (Unsafe) f.get(null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final int base = unsafe.arrayBaseOffset(long[].class);
    private static final int shift;

    static {
        int scale = unsafe.arrayIndexScale(long[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }

    private static void increment(long[] array, int index){
        long offset = byteOffset(index);
        while (true) {
            long current = unsafe.getLongVolatile(array, offset);
            if (unsafe.compareAndSwapLong(array, offset, current, current + 1)){
                break;
            }
        }
    }

    private SimpleProfiler(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int topSize = 10;
                int[] topMethodId = new int[topSize];
                for(int i=0; i<methodListCount; i++){
                    if(invocationMethodCount[topMethodId[0]] < invocationMethodCount[i]){
                        System.arraycopy(topMethodId, 0, topMethodId, 1, topMethodId.length-1);
                        topMethodId[0] = i;
                    }
                }
                for (int i = 0; i < topMethodId.length; i++) {
                    int methodId = topMethodId[i];
                    if (invocationMethodCount[methodId] > 0) {
                        System.out.println(" - top ["+i+"] method [" + methodList[methodId].getDeclaringClass().getName() +"#"+ methodList[methodId].getName() + "] invocations [" + invocationMethodCount[methodId] + "]");
                    }
                }

                int[] topClassId = new int[topSize];
                for(int i=0; i<classListCount; i++){
                    if(invocationClassCount[topClassId[0]] < invocationClassCount[i]){
                        System.arraycopy(topClassId, 0, topClassId, 1, topClassId.length-1);
                        topClassId[0] = i;
                    }
                }
                for (int i = 0; i < topClassId.length; i++) {
                    int classId = topClassId[i];
                    if (invocationClassCount[classId] > 0) {
                        System.out.println(" - top ["+i+"] class [" + classList[classId].getName() + "] invocations [" + invocationClassCount[classId] + "]");
                    }
                }
            }
        }, 10000, 10000);
    }

    public synchronized int registerClass(CtClass ctClass){
        classListCount++;
        if(classListCount >= classList.length){
            // resize class list
            CtClass[] newClassList = new CtClass[classList.length+1000];
            System.arraycopy(classList, 0, newClassList, 0, classList.length);
            classList = newClassList;
            // resize invocation class count
            long[] newInvocationClassCount = new long[invocationClassCount.length+1000];
            System.arraycopy(invocationClassCount, 0, newInvocationClassCount, 0, invocationClassCount.length);
            invocationClassCount = newInvocationClassCount;
        }
        classList[classListCount] = ctClass;
        return classListCount;
    }

    public synchronized int registerMethod(CtMethod ctMethod){
        methodListCount++;
        if(methodListCount >= methodList.length){
            // resize method list
            CtMethod[] newMethodList = new CtMethod[methodList.length+1000];
            System.arraycopy(methodList, 0, newMethodList, 0, methodList.length);
            methodList = newMethodList;
            // resize invocation count
            long[] newInvocationMethodCount = new long[invocationMethodCount.length+1000];
            System.arraycopy(invocationMethodCount, 0, newInvocationMethodCount, 0, invocationMethodCount.length);
            invocationMethodCount = newInvocationMethodCount;
        }
        methodList[methodListCount] = ctMethod;
        return methodListCount;
    }

    public void push(int classId, int methodId, long nanoTime){
        increment(invocationClassCount, classId);
        increment(invocationMethodCount, methodId);
    }

    public void pop(int classId, int methodId, long nanoTime){
    }
}
