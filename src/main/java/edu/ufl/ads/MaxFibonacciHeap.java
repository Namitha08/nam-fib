package edu.ufl.ads;

import java.util.*;

public class MaxFibonacciHeap {

    private Node maxNode = null; //Maximum node of the heap.

    /**
     * Insert element to the heap
     * @param value new node value
     * @return Node which got created in heap
     */
    public Node enqueue(double value) {
        Node result = new Node(value);
        maxNode = mergeLists(maxNode, result); //add new node to the existing tree
        return result;
    }

    /**
     * Get maximum element of the heap
     * @return maximum element of the heap
     */
    public Node max() {
        if (maxNode == null)
            throw new IllegalAccessError("Trying to access empty heap.");
        return maxNode;
    }

    /**
     * Dequeue's the max element of the heap
     *
     * @return max element of the heap
     */
    public Node dequeueMax() {
        if (maxNode == null) //isHeap empty?
            throw new IllegalAccessError("Heap is empty.");

        Node maxElem = maxNode;

        /*
         * Otherwise, if it's not null, then we write the elements next to the
         * min element around the min element to remove it, then arbitrarily
         * reassign the min.
         */
        if (maxNode.next == maxNode) { // max element doesn't have siblings assign max to null
            maxNode = null;
        }
        else { // assign random node to max
            maxNode.prev.next = maxNode.next;
            maxNode.next.prev = maxNode.prev;
            maxNode = maxNode.next;
        }

        /* Next, clear the parent fields of all of the max element's children,
         * since they're about to become roots.  Because the elements are
         * stored in a circular list, the traversal is a bit complex.
         */
        if (maxElem.child != null) {
            /* Keep track of the first visited node. */
            Node curr = maxElem.child;
            do {
                curr.parent = null;

                /* Walk to the next node, then stop if this is the node we
                 * started at.
                 */
                curr = curr.next;
            } while (curr != maxElem.child);
        }

        /* Next, splice the children of the root node into the topmost list, 
         * then set maxNode to point somewhere in that list.
         */
        maxNode = mergeLists(maxNode, maxElem.child);

        /* If there are no entries left, we're done. */
        if (maxNode == null) return maxElem;

        /* Next, we need to coalsce all of the roots so that there is only one
         * tree of each degree.  To track trees of each size, we allocate an
         * ArrayList where the entry at position i is either null or the 
         * unique tree of degree i.
         */
        List<Node> treeTable = new ArrayList<Node>();

        /* We need to traverse the entire list, but since we're going to be
         * messing around with it we have to be careful not to break our
         * traversal order mid-stream.  One major challenge is how to detect
         * whether we're visiting the same node twice.  To do this, we'll
         * spent a bit of overhead adding all of the nodes to a list, and
         * then will visit each element of this list in order.
         */
        List<Node> toVisit = new ArrayList<Node>();

        /* To add everything, we'll iterate across the elements until we
         * find the first element twice.  We check this by looping while the
         * list is empty or while the current element isn't the first element
         * of that list.
         */
        for (Node curr = maxNode; toVisit.isEmpty() || toVisit.get(0) != curr; curr = curr.next)
            toVisit.add(curr);

        /* Traverse this list and perform the appropriate unioning steps. */
        for (Node curr: toVisit) {
            /* Keep merging until a match arises. */
            while (true) {
                /* Ensure that the list is long enough to hold an element of this
                 * degree.
                 */
                while (curr.degree >= treeTable.size())
                    treeTable.add(null);

                /* If nothing's here, we're can record that this tree has this size
                 * and are done processing.
                 */
                if (treeTable.get(curr.degree) == null) {
                    treeTable.set(curr.degree, curr);
                    break;
                }

                /* Otherwise, merge with what's there. */
                Node other = treeTable.get(curr.degree);
                treeTable.set(curr.degree, null); // Clear the slot

                /* Determine which of the two trees has the smaller root, storing
                 * the two tree accordingly.
                 */
                Node max = (other.value >= curr.value)? other : curr;
                Node min = (other.value >= curr.value)? curr  : other;

                /* Break min out of the root list, then merge it into max's child
                 * list.
                 */
                min.next.prev = min.prev;
                min.prev.next = min.next;

                /* Make it a singleton so that we can merge it. */
                min.next = min.prev = min;
                max.child = mergeLists(max.child, min);
                
                /* Reparent min appropriately. */
                min.parent = max;

                /* Clear min's mark, since it can now lose another child. */
                min.childCut = false;

                /* Increase max's degree; it now has another child. */
                ++max.degree;

                /* Continue merging this tree. */
                curr = max;
            }

            /* Update the global min based on this node.  Note that we compare
             * for <= instead of < here.  That's because if we just did a
             * reparent operation that merged two different trees of equal
             * priority, we need to make sure that the min pointer points to
             * the root-level one.
             */
            if (curr.value >= maxNode.value) maxNode = curr;
        }
        return maxElem;
    }

    public void increaseKey(Node node, double newValue) {
        if (newValue < node.value)
            throw new IllegalArgumentException("New value exceeds old.");

        increaseKeyValue(node, newValue);
    }

    public void delete(Node node) {
        //Simple hack to remove the node, assign node value to max and deque max element
        increaseKeyValue(node, Double.MAX_VALUE);
        dequeueMax();
    }


    /**
     * Merges two lists to single list and returns max node
     * @param one first tree head node
     * @param two second tree head node
     * @return max node of the merged list
     */
    private static Node mergeLists(Node one, Node two) {

        if (one == null && two == null) {
            return null;
        }
        else if (one != null && two == null) {
            return one;
        }
        else if (one == null && two != null) {
            return two;
        }
        else {  // join two linked lists
            Node oneNext = one.next;
            one.next = two.next;
            one.next.prev = one;
            two.next = oneNext;
            two.next.prev = two;

            return one.value > two.value ? one : two; //return max node
        }
    }

    /**
     *
     * @param node The node whose key should be decreased.
     * @param priority The node's new priority.
     */
    private void increaseKeyValue(Node node, double priority) {
        /* First, change the node's priority. */
        node.value = priority;

        /* If the node no longer has a higher priority than its parent, cut it.
         * Note that this also means that if we try to run a delete operation
         * that decreases the key to -infinity, it's guaranteed to cut the node
         * from its parent.
         */
        if (node.parent != null && node.value >= node.parent.value)
            cutNode(node);

        /* If our new value is the new min, mark it as such.  Note that if we
         * ended up decreasing the key in a way that ties the current minimum
         * priority, this will change the min accordingly.
         */
        if (node.value >= maxNode.value)
            maxNode = node;
    }

    /**
     * Cuts a node from its parent.  If the parent was already marked, recursively
     * cuts that node from its parent as well.
     *
     * @param node The node to cut from its parent.
     */
    private void cutNode(Node node) {
        /* Begin by clearing the node's mark, since we just cut it. */
        node.childCut = false;

        /* Base case: If the node has no parent, we're done. */
        if (node.parent == null) return;

        /* Rewire the node's siblings around it, if it has any siblings. */
        if (node.next != node) { // Has siblings
            node.next.prev = node.prev;
            node.prev.next = node.next;
        }

        /* If the node is the one identified by its parent as its child,
         * we need to rewrite that pointer to point to some arbitrary other
         * child.
         */
        if (node.parent.child == node) {
            /* If there are any other children, pick one of them arbitrarily. */
            if (node.next != node) {
                node.parent.child = node.next;
            }
            /* Otherwise, there aren't any children left and we should clear the
             * pointer and drop the node's degree.
             */
            else {
                node.parent.child = null;
            }
        }

        /* Decrease the degree of the parent, since it just lost a child. */
        --node.parent.degree;

        /* Splice this tree into the root list by converting it to a singleton
         * and invoking the merge subroutine.
         */
        node.prev = node.next = node;
        maxNode = mergeLists(maxNode, node);

        /* Mark the parent and recursively cut it if it's already been
         * marked.
         */
        if (node.parent.childCut)
            cutNode(node.parent);
        else
            node.parent.childCut = true;

        /* Clear the relocated node's parent; it's now a root. */
        node.parent = null;
    }
}