package com.dungeoncrawler.generator.search;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generalized A* priority queue
 */
public class PriorityQueue<A extends IHeuristic<A>> extends SearchCollection<A> {
    private Map<A, Float> f;
    private LinkedHashMap<A, Float> D;
    private Node<A> goal;

    public PriorityQueue() {
        super(new Queue<A>());
        D = new LinkedHashMap<A, Float>();
        f = new HashMap<A, Float>();
    }

    @Override
    public boolean pushChild(Node<A> a, Node<A> parent) {
        Float parentD = D.get(parent.getData());
        if (parentD == null)
            parentD = Float.MAX_VALUE;

        float h = a.calculateHeuristic(parent);
        float cost = parentD + h;
        if (!collection.contains(a) || cost < parentD) {
            D.put(parent.getData(), cost);
            f.put(parent.getData(), D.get(parent.getData()) + parent.calculateHeuristic(goal));
            if (!collection.contains(a))
                collection.push(a);

            return true;
        }

        return false;
    }

    @Override
    public void push(Node<A> a, boolean isStart) {
        super.push(a, isStart);
        if (isStart) {
            goal = a;
            D.put(a.getData(), 0f);
            f.put(a.getData(), a.calculateHeuristic(goal));
        }
    }

    @Override
    public Node<A> pop() {
        float minH = Float.MAX_VALUE;
        Node<A> minA = null;

        for (Object o : collection) {
            Node<A> a = (Node<A>) o;

            Float aHeuristic = f.get(a.getData());

            if (aHeuristic != null && aHeuristic < minH) {
                minH = aHeuristic;
                minA = a;
            }
        }

        if (minA == null)
            minA = (Node<A>) collection.pop();
        else
            collection.remove(minA);

        return minA;
    }
}
