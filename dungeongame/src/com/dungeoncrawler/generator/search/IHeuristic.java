package com.dungeoncrawler.generator.search;

public interface IHeuristic<A> {
    float calculate(A goal);
}
