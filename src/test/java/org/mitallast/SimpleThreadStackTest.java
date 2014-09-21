package org.mitallast;

import org.junit.Assert;
import org.junit.Test;

public class SimpleThreadStackTest {
    @Test
    public void oneMethod() {
        SimpleThreadStack threadStack = new SimpleThreadStack();
        threadStack.push(1, 1, 1000);
        Assert.assertEquals(1000, threadStack.pop(1, 1, 2000));
    }

    @Test
    public void twoMethods() {
        SimpleThreadStack threadStack = new SimpleThreadStack();
        threadStack.push(1, 1, 1000);
        threadStack.push(1, 2, 2000);
        Assert.assertEquals(1000, threadStack.pop(1, 2, 3000));
        Assert.assertEquals(2000, threadStack.pop(1, 1, 4000));
    }
}
