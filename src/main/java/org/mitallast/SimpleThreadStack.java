package org.mitallast;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleThreadStack {

    private final Map<Long, ThreadStack> threadMap = new ConcurrentHashMap<>();

    /**
     * @param classId   class id
     * @param methodId  method id
     * @param startTime start time in nano
     */
    public void push(int classId, int methodId, long startTime) {
        long tid = Thread.currentThread().getId();
        ThreadStack stack = threadMap.get(tid);
        if (stack == null) {
            stack = new ThreadStack(tid);
            threadMap.put(tid, stack);
        }
        stack.push(classId, methodId, startTime);
    }

    /**
     * @param classId  class id
     * @param methodId method id
     * @param endTime  end time in nano
     * @return self method time
     */
    public long pop(int classId, int methodId, long endTime) {
        long tid = Thread.currentThread().getId();
        ThreadStack stack = threadMap.get(tid);
        long time = stack.pop(classId, methodId, endTime);
        if (stack.isEmpty()) {
            threadMap.remove(tid);
        }
        return time;
    }

    private static class ThreadStack {
        private final static int maxStackSize = 255;
        private final long threadId;
        private final MethodCall[] stack = new MethodCall[maxStackSize];
        private int stackSize = 0;

        private ThreadStack(long threadId) {
            this.threadId = threadId;
        }

        /**
         * @param classId   class id
         * @param methodId  method id
         * @param startTime start time in nano
         */
        public void push(int classId, int methodId, long startTime) {
            MethodCall methodCall = new MethodCall(classId, methodId, startTime);
            if (stackSize >= maxStackSize) {
                throw new RuntimeException("Stack overflow");
            }
            stack[stackSize] = methodCall;
            stackSize++;
        }

        /**
         * @param classId  class id
         * @param methodId method id
         * @param endTime  end time in nano
         * @return self time of method
         */
        public long pop(int classId, int methodId, long endTime) {
            if (stackSize == 0) {
                throw new RuntimeException("Stack is empty");
            }
            stackSize--;
            MethodCall current = stack[stackSize];
            if (!current.equals(classId, methodId)) {
                throw new RuntimeException("Thread stack is corrupted\n"
                    + "Expected class=" + classId + " method=" + endTime + "\n"
                    + "Stack: " + toString());
            }
            stack[stackSize] = null;
            long currentTime = current.time(endTime);
            if (stackSize > 0) {
                stack[stackSize - 1].decrease(currentTime);
            }
            return currentTime;
        }

        public boolean isEmpty() {
            return stackSize == 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ThreadStack stack = (ThreadStack) o;
            return threadId == stack.threadId;
        }

        @Override
        public int hashCode() {
            return (int) (threadId ^ (threadId >>> 32));
        }

        @Override
        public String toString() {
            return "ThreadStack{" +
                "threadId=" + threadId +
                ", stack=" + Arrays.toString(stack) +
                ", stackSize=" + stackSize +
                '}';
        }
    }

    private static class MethodCall {
        private final int classId;
        private final int methodId;
        private final long start;
        private long time = 0;

        /**
         * @param classId   class id
         * @param methodId  method id
         * @param startTime start time in nano
         */
        public MethodCall(int classId, int methodId, long startTime) {
            this.classId = classId;
            this.methodId = methodId;
            this.start = startTime;
        }

        public boolean equals(int classId, int methodId) {
            return this.classId == classId
                && this.methodId == methodId;
        }

        /**
         * @param nanoTime time of internal call, will be excluded from self time of method in nano
         */
        public void decrease(long nanoTime) {
            time += nanoTime;
        }

        /**
         * @param endTime time of last operation in method in nano
         * @return self time of method, without of time internal calls another methods
         */
        public long time(long endTime) {
            return endTime - start - time;
        }

        @Override
        public String toString() {
            return "MethodCall{" +
                "classId=" + classId +
                ", methodId=" + methodId +
                ", start=" + start +
                ", time=" + time +
                "}\n";
        }
    }
}
