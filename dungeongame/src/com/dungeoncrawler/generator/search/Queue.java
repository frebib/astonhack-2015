package com.dungeoncrawler.generator.search;

import java.util.LinkedList;

public class Queue<A> extends GenericCollection<java.util.Queue<A>, A> {

    public Queue() {
        super(new LinkedList<A>());
    }

    @Override
    public A pop() {
        return collection.poll();
    }

    @Override
    public void push(A item) {
        collection.add(item);
    }

}
