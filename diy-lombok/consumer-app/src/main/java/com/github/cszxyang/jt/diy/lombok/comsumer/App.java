package com.github.cszxyang.jt.diy.lombok.comsumer;

public class App {
    public static void main(String[] args) {
        TestingEntity entity = new TestingEntity();
        entity.setIntVal(10);
        entity.setStrVal("hello lombok");
        System.out.println(entity.getIntVal() + " " + entity.getStrVal());
    }
}
