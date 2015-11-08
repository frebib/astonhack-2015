package com.dungeoncrawler.generator.search.maybe;


/**
 * Interface for the Maybe type using the "composite pattern".
 * We include high-order methods.
 * We will use A,B,C for type variables.
 */

public interface Maybe<A> {
    boolean isNothing();

    int size();

    boolean has(A a);

    // Higher-order methods:
    Maybe<A> filter(Predicate<A> p);

    <B> Maybe<B> map(Function<A, B> f);

    <B> B fold(Function<A, B> f, B b);

    boolean all(Predicate<A> p);

    boolean some(Predicate<A> p);

    void forEach(Action<A> a);

    A fromMaybe();
}