package org.mitallast;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class SimpleProfile {
    private static final int chunkSize = 1000;

    private static final Unsafe unsafe;
    private static final int base;
    private static final int shift;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        base = unsafe.arrayBaseOffset(long[].class);
        int scale = unsafe.arrayIndexScale(long[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }

    private volatile long[] profile = new long[chunkSize];
    private volatile int profileCount = 0;

    public int size() {
        return profile.length;
    }

    public void resize(int count) {
        if (count >= profile.length) {
            int newSize = Math.max(profile.length + chunkSize, count + chunkSize);
            long[] newProfile = new long[newSize];
            System.arraycopy(profile, 0, newProfile, 0, profile.length);
            profile = newProfile;
        }
        profileCount = count;
    }

    public void increment(int index) {
        long offset = byteOffset(index);
        while (true) {
            long current = unsafe.getLongVolatile(profile, offset);
            if (unsafe.compareAndSwapLong(profile, offset, current, current + 1)) {
                break;
            }
        }
    }

    public void increase(int index, long value) {
        long offset = byteOffset(index);
        while (true) {
            long current = unsafe.getLongVolatile(profile, offset);
            if (unsafe.compareAndSwapLong(profile, offset, current, current + value)) {
                break;
            }
        }
    }

    public long get(int index) {
        long offset = byteOffset(index);
        return unsafe.getLongVolatile(profile, offset);
    }

    public int[] top(int topSize) {
        topSize = Math.min(profileCount, topSize);
        int[] top = new int[topSize];
        for (int i = 0; i < profileCount; i++) {
            if (profile[top[0]] < profile[i]) {
                System.arraycopy(top, 0, top, 1, top.length - 1);
                top[0] = i;
            }
        }
        return top;
    }
}
