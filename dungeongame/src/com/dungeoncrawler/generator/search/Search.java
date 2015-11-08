package com.dungeoncrawler.generator.search;

import com.dungeoncrawler.generator.search.maybe.*;

import java.util.*;

/**
 * The search algorithm depends on the supplied data structure
 */
public class Search<A extends IHeuristic<A>> {
    private SearchCollection<A> frontier;
    protected List<Node<A>> nodes;
    protected Set<Node<A>> visited;

    public Search(SearchCollection<A> frontier, List<Node<A>> nodes) {
        this.frontier = frontier;
        this.nodes = nodes;
        this.visited = new HashSet<Node<A>>();
    }

    /**
     * Finds a path from between the given node and goal node that the given predicate holds for
     *
     * @param x Starting node
     * @param p Predicate that holds for the goal node. Must be a CoordinatePredicate for A*
     * @return Maybe a List of Nodes to the goal node
     */
    public Maybe<List<Node<A>>> findPathFrom(Node<A> x, Predicate<A> p) {
        LinkedHashMap<Node<A>, Node<A>> parentTraceMap = new LinkedHashMap<Node<A>, Node<A>>();
        Maybe<Node<A>> found = findNode(x, p, parentTraceMap);

        if (found.isNothing())
            return new Nothing<List<Node<A>>>();


        List<Node<A>> path = tracePath(found.fromMaybe(), parentTraceMap);
        return new Just<List<Node<A>>>(path);
    }

    /**
     * Finds a node that the given predicate holds for
     *
     * @param x Starting node
     * @param p Predicate that holds for the goal node. Must be a CoordinatePredicate for A*
     * @return Maybe the goal node
     */
    public Maybe<Node<A>> findNodeFrom(Node<A> x, Predicate<A> p) {
        return findNode(x, p, null);
    }


    /**
     * @param start       Start node
     * @param predicate   Predicate that holds for the goal node
     * @param parentTrace Used for path tracking: can be null
     * @return Maybe the goal node
     */
    private Maybe<Node<A>> findNode(Node<A> start, Predicate<A> predicate, LinkedHashMap<Node<A>, Node<A>> parentTrace) {
        frontier.clear();
        visited.clear();
        frontier.push(start, true);

        while (!frontier.isEmpty()) {
            Node<A> node = frontier.pop();

            // visit check
            if (!visited.contains(node)) {
                visited.add(node);

                // goal check
                if (predicate.holds(node.getData()))
                    return new Just<Node<A>>(node);

                // add children
                for (Node<A> child : node)
                    if (!visited.contains(child))
                        if (frontier.pushChild(child, node) && parentTrace != null)
                            parentTrace.put(child, node);
            }
        }

        return new Nothing<Node<A>>();
    }

    /**
     * @param node The goal node
     * @param path Tracked path of child: parent
     * @return The path
     */
    private List<Node<A>> tracePath(Node<A> node, Map<Node<A>, Node<A>> path) {
        List<Node<A>> completePath = new ArrayList<Node<A>>();
        completePath.add(node);

        while (path.containsKey(node)) {
            node = path.get(node);
            completePath.add(node);
        }

        reverse(completePath);
        return completePath;
    }

    /**
     * Reverses a list
     */
    private void reverse(List<Node<A>> list) {
        for (int i = 0; i < list.size() / 2; i++) {
            Node<A> temp = list.get(i);
            int j = list.size() - i - 1;

            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }

    @Override
    public String toString() {
        String name = frontier.getClass().getSimpleName();
        int index = 0;
        int capsCount = 2;
        for (int i = 0; i < name.length(); i++) {
            if (Character.isUpperCase(name.charAt(i))) {
                if (capsCount-- <= 0) {
                    index = i;
                    break;
                }
            }
        }

        return capsCount <= 0 ? name.substring(index) : name;
    }
}
