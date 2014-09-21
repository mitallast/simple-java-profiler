package org.mitallast;

import java.lang.instrument.Instrumentation;

public class SimpleAgent {

    private static Instrumentation instrumentation = null;

    public static void premain(String argument, Instrumentation instrumentation) {
        System.out.println("premain called");
        SimpleClassTransformer transformer = new SimpleClassTransformer();
        instrumentation.addTransformer(transformer);
    }

    public static void agentmain(String argument, Instrumentation instrumentation) {
        System.out.println("agentmain called");
//        SimpleClassTransformer transformer = new SimpleClassTransformer();
//        instrumentation.addTransformer(transformer);
    }
}
