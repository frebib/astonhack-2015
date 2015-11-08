package com.dungeoncrawler.generator.search.maybe;

public interface Predicate<A> {
    boolean holds(A a);
}
