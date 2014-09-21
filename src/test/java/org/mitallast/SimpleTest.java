package org.mitallast;

public class SimpleTest{
    public static void main(String[] args) {
        Test1 one = new Test1();
        Test2 two = new Test2();
        System.out.println("Simple Main " + one + " " + two);
        foo();
    }

    public static int foo(){return 123;}
}

class Test1{}
class Test2{}
