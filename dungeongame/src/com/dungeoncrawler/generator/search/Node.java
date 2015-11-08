package com.dungeoncrawler.generator.search;

import java.util.ArrayList;
import java.util.Iterator;

public class Node<A extends IHeuristic<A>> implements Iterable<Node<A>> {
    private A data;
    private ArrayList<Node<A>> children;

    public Node(A data) {
        this.data = data;
        this.children = new ArrayList<Node<A>>();
    }

    public A getData() {
        return data;
    }

    public void addChild(Node<A> node) {
        children.add(node);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node<A> other = (Node<A>) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }

    public String toString() {
        return String.format("Node(%s)", data);
    }

    @Override
    public Iterator<Node<A>> iterator() {
        return new NodeIterator<A>(this);
    }

    public ArrayList<Node<A>> getChildren() {
        return children;
    }

    public float calculateHeuristic(Node<A> goal) {
        return data.calculate(goal.getData());
    }

    private class NodeIterator<A extends IHeuristic<A>> implements Iterator<Node<A>> {
        private final ArrayList<Node<A>> children;
        private int index;

        public NodeIterator(Node<A> node) {
            this.index = 0;
            this.children = node.children;
        }

        @Override
        public boolean hasNext() {
            return index < children.size();
        }

        @Override
        public Node<A> next() {
            return children.get(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}