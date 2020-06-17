package com.cszxyang.jt.javac.tree;

import java.util.concurrent.atomic.AtomicInteger;

public class SeqGen {
    private AtomicInteger initialVal;

    public SeqGen(int initialVal) {
        this.initialVal = new AtomicInteger(initialVal);
    }

    public String next() {
        this.initialVal.getAndIncrement();
        return String.valueOf(this.initialVal.get());
    }
}
