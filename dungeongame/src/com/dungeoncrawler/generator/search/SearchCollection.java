package com.dungeoncrawler.generator.search;

/**
 * Base data structure for generalized search
 */
public abstract class SearchCollection<A extends IHeuristic<A>> {
    protected GenericCollection collection;

    public SearchCollection(GenericCollection collection) {
        this.collection = collection;
    }

    /**
     * @param a      The child node
     * @param parent The child's parent
     * @return True if the child was pushed onto the frontier, otherwise false
     */
    public boolean pushChild(Node<A> a, Node<A> parent) {
        push(a, false);
        return true;
    }

    /**
     * @param a       The node to push
     * @param isStart Whether this is the first node to be pushed on
     */
    public void push(Node<A> a, boolean isStart) {
        collection.push(a);
    }

    /**
     * @return The next node off the frontier
     */
    public Node<A> pop() {
        return (Node<A>) collection.pop();
    }


    public boolean isEmpty() {
        return collection.isEmpty();
    }

    public void clear() {
        collection.clear();
    }

    @Override
    public String toString() {
        return collection.toString();
    }
}
