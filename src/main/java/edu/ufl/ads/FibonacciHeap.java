package edu.ufl.ads;
/***********************************************************************
 * File: edu.ufl.ads.FibonacciHeap.java
 * Author: Keith Schwarz (htiek@cs.stanford.edu)
 *
 * An implementation of a priority queue backed by a Fibonacci heap,
 * as described by Fredman and Tarjan.  Fibonacci heaps are interesting
 * theoretically because they have asymptotically good runtime guarantees
 * for many operations.  In particular, insert, peek, and decrease-key all
 * run in amortized O(1) time.  dequeueMax and delete each run in amortized
 * O(lg n) time.  This allows algorithms that rely heavily on decrease-key
 * to gain significant performance boosts.  For example, Dijkstra's algorithm
 * for single-source shortest paths can be shown to run in O(m + n lg n) using
 * a Fibonacci heap, compared to O(m lg n) using a standard binary or binomial
 * heap.
 *
 * Internally, a Fibonacci heap is represented as a circular, doubly-linked
 * list of trees obeying the min-heap property.  Each node stores pointers
 * to its parent (if any) and some arbitrary child.  Additionally, every
 * node stores its degree (the number of children it has) and whether it
 * is a "marked" node.  Finally, each Fibonacci heap stores a pointer to
 * the tree with the minimum value.
 *
 * To insert a node into a Fibonacci heap, a singleton tree is created and
 * merged into the rest of the trees.  The merge operation works by simply
 * splicing together the doubly-linked lists of the two trees, then updating
 * the min pointer to be the smaller of the minima of the two heaps.  Peeking
 * at the smallest element can therefore be accomplished by just looking at
 * the min element.  All of these operations complete in O(1) time.
 *
 * The tricky operations are dequeueMax and decreaseKey.  dequeueMax works
 * by removing the root of the tree containing the smallest element, then
 * merging its children with the topmost roots.  Then, the roots are scanned
 * and merged so that there is only one tree of each degree in the root list.
 * This works by maintaining a dynamic array of trees, each initially null,
 * pointing to the roots of trees of each dimension.  The list is then scanned
 * and this array is populated.  Whenever a conflict is discovered, the
 * appropriate trees are merged together until no more conflicts exist.  The
 * resulting trees are then put into the root list.  A clever analysis using
 * the potential method can be used to show that the amortized cost of this
 * operation is O(lg n), see "Introduction to Algorithms, Second Edition" by
 * Cormen, Rivest, Leiserson, and Stein for more details.
 *
 * The other hard operation is decreaseKey, which works as follows.  First, we
 * update the key of the node to be the new value.  If this leaves the node
 * smaller than its parent, we're done.  Otherwise, we cut the node from its
 * parent, add it as a root, and then mark its parent.  If the parent was
 * already marked, we cut that node as well, recursively mark its parent,
 * and continue this process.  This can be shown to run in O(1) amortized time
 * using yet another clever potential function.  Finally, given this function,
 * we can implement delete by decreasing a key to -\infty, then calling
 * dequeueMax to extract it.
 */

import java.util.*; // For ArrayList

/**
 * A class representing a Fibonacci heap.
 *
 * @author Keith Schwarz (htiek@cs.stanford.edu)
 */
public class FibonacciHeap {

    /* Pointer to the minimum element in the heap. */
    private Node mMax = null;

    /* Cached size of the heap, so we don't have to recompute this explicitly. */
    private int mSize = 0;

    /**
     * Inserts the specified element into the Fibonacci heap with the specified
     * priority.  Its priority must be a valid double, so you cannot set the
     * priority to NaN.
     *
     * @param priority Its priority, which must be valid.
     * @return An Node representing that element in the tree.
     */
    public Node enqueue(double priority) {
        checkPriority(priority);

        /* Create the entry object, which is a circularly-linked list of length
         * one.
         */
        Node result = new Node(priority);

        /* Merge this singleton list with the tree list. */
        mMax = mergeLists(mMax, result);

        /* Increase the size of the heap; we just added something. */
        ++mSize;

        /* Return the reference to the new element. */
        return result;
    }

    /**
     * Returns an Node object corresponding to the minimum element of the
     * Fibonacci heap, throwing a NoSuchElementException if the heap is
     * empty.
     *
     * @return The smallest element of the heap.
     * @throws NoSuchElementException If the heap is empty.
     */
    public Node max() {
        if (isEmpty())
            throw new NoSuchElementException("Heap is empty.");
        return mMax;
    }

    /**
     * Returns whether the heap is empty.
     *
     * @return Whether the heap is empty.
     */
    public boolean isEmpty() {
        return mMax == null;
    }

    /**
     * Returns the number of elements in the heap.
     *
     * @return The number of elements in the heap.
     */
    public int size() {
        return mSize;
    }

    /**
     * Given two Fibonacci heaps, returns a new Fibonacci heap that contains
     * all of the elements of the two heaps.  Each of the input heaps is
     * destructively modified by having all its elements removed.  You can
     * continue to use those heaps, but be aware that they will be empty
     * after this call completes.
     *
     * @param one The first Fibonacci heap to merge.
     * @param two The second Fibonacci heap to merge.
     * @return A new edu.ufl.ads.FibonacciHeap containing all of the elements of both
     *         heaps.
     */
    public static FibonacciHeap merge(FibonacciHeap one, FibonacciHeap two) {
        /* Create a new edu.ufl.ads.FibonacciHeap to hold the result. */
        FibonacciHeap result = new FibonacciHeap();

        /* Merge the two Fibonacci heap root lists together.  This helper function
         * also computes the min of the two lists, so we can store the result in
         * the mMax field of the new heap.
         */
        result.mMax = mergeLists(one.mMax, two.mMax);

        /* The size of the new heap is the sum of the sizes of the input heaps. */
        result.mSize = one.mSize + two.mSize;

        /* Clear the old heaps. */
        one.mSize = two.mSize = 0;
        one.mMax = null;
        two.mMax = null;

        /* Return the newly-merged heap. */
        return result;
    }

    /**
     * Dequeues and returns the minimum element of the Fibonacci heap.  If the
     * heap is empty, this throws a NoSuchElementException.
     *
     * @return The smallest element of the Fibonacci heap.
     * @throws NoSuchElementException If the heap is empty.
     */
    public Node dequeueMax() {
        /* Check for whether we're empty. */
        if (isEmpty())
            throw new NoSuchElementException("Heap is empty.");

        /* Otherwise, we're about to lose an element, so decrement the number of
         * entries in this heap.
         */
        --mSize;

        /* Grab the minimum element so we know what to return. */
        Node maxElem = mMax;

        /* Now, we need to get rid of this element from the list of roots.  There
         * are two cases to consider.  First, if this is the only element in the
         * list of roots, we set the list of roots to be null by clearing mMax.
         * Otherwise, if it's not null, then we write the elements next to the
         * min element around the min element to remove it, then arbitrarily
         * reassign the min.
         */
        if (mMax.mNext == mMax) { // Case one
            mMax = null;
        }
        else { // Case two
            mMax.mPrev.mNext = mMax.mNext;
            mMax.mNext.mPrev = mMax.mPrev;
            mMax = mMax.mNext; // Arbitrary element of the root list.
        }

        /* Next, clear the parent fields of all of the max element's children,
         * since they're about to become roots.  Because the elements are
         * stored in a circular list, the traversal is a bit complex.
         */
        if (maxElem.mChild != null) {
            /* Keep track of the first visited node. */
            Node curr = maxElem.mChild;
            do {
                curr.mParent = null;

                /* Walk to the next node, then stop if this is the node we
                 * started at.
                 */
                curr = curr.mNext;
            } while (curr != maxElem.mChild);
        }

        /* Next, splice the children of the root node into the topmost list, 
         * then set mMax to point somewhere in that list.
         */
        mMax = mergeLists(mMax, maxElem.mChild);

        /* If there are no entries left, we're done. */
        if (mMax == null) return maxElem;

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
        for (Node curr = mMax; toVisit.isEmpty() || toVisit.get(0) != curr; curr = curr.mNext)
            toVisit.add(curr);

        /* Traverse this list and perform the appropriate unioning steps. */
        for (Node curr: toVisit) {
            /* Keep merging until a match arises. */
            while (true) {
                /* Ensure that the list is long enough to hold an element of this
                 * degree.
                 */
                while (curr.mDegree >= treeTable.size())
                    treeTable.add(null);

                /* If nothing's here, we're can record that this tree has this size
                 * and are done processing.
                 */
                if (treeTable.get(curr.mDegree) == null) {
                    treeTable.set(curr.mDegree, curr);
                    break;
                }

                /* Otherwise, merge with what's there. */
                Node other = treeTable.get(curr.mDegree);
                treeTable.set(curr.mDegree, null); // Clear the slot

                /* Determine which of the two trees has the smaller root, storing
                 * the two tree accordingly.
                 */
                Node max = (other.mValue >= curr.mValue)? other : curr;
                Node min = (other.mValue >= curr.mValue)? curr  : other;

                /* Break min out of the root list, then merge it into max's child
                 * list.
                 */
                min.mNext.mPrev = min.mPrev;
                min.mPrev.mNext = min.mNext;

                /* Make it a singleton so that we can merge it. */
                min.mNext = min.mPrev = min;
                max.mChild = mergeLists(max.mChild, min);
                
                /* Reparent min appropriately. */
                min.mParent = max;

                /* Clear min's mark, since it can now lose another child. */
                min.mIsMarked = false;

                /* Increase max's degree; it now has another child. */
                ++max.mDegree;

                /* Continue merging this tree. */
                curr = max;
            }

            /* Update the global min based on this node.  Note that we compare
             * for <= instead of < here.  That's because if we just did a
             * reparent operation that merged two different trees of equal
             * priority, we need to make sure that the min pointer points to
             * the root-level one.
             */
            if (curr.mValue >= mMax.mValue) mMax = curr;
        }
        return maxElem;
    }

    /**
     * Decreases the key of the specified element to the new priority.  If the
     * new priority is greater than the old priority, this function throws an
     * IllegalArgumentException.  The new priority must be a finite double,
     * so you cannot set the priority to be NaN, or +/- infinity.  Doing
     * so also throws an IllegalArgumentException.
     *
     * It is assumed that the node belongs in this heap.  For efficiency
     * reasons, this is not checked at runtime.
     *
     * @param node The element whose priority should be decreased.
     * @param newPriority The new priority to associate with this node.
     * @throws IllegalArgumentException If the new priority exceeds the old
     *         priority, or if the argument is not a finite double.
     */
    public void decreaseKey(Node node, double newPriority) {
        checkPriority(newPriority);
        if (newPriority < node.mValue)
            throw new IllegalArgumentException("New priority exceeds old.");

        /* Forward this to a helper function. */
        decreaseKeyUnchecked(node, newPriority);
    }
    
    /**
     * Deletes this Node from the Fibonacci heap that contains it.
     *
     * It is assumed that the node belongs in this heap.  For efficiency
     * reasons, this is not checked at runtime.
     *
     * @param node The node to delete.
     */
    public void delete(Node node) {
        /* Use decreaseKey to drop the node's key to -infinity.  This will
         * guarantee that the node is cut and set to the global minimum.
         */
        decreaseKeyUnchecked(node, Double.NEGATIVE_INFINITY);

        /* Call dequeueMax to remove it. */
        dequeueMax();
    }

    /**
     * Utility function which, given a user-specified priority, checks whether
     * it's a valid double and throws an IllegalArgumentException otherwise.
     *
     * @param priority The user's specified priority.
     * @throws IllegalArgumentException If it is not valid.
     */
    private void checkPriority(double priority) {
        if (Double.isNaN(priority))
            throw new IllegalArgumentException(priority + " is invalid.");
    }

    /**
     * Utility function which, given two pointers into disjoint circularly-
     * linked lists, merges the two lists together into one circularly-linked
     * list in O(1) time.  Because the lists may be empty, the return value
     * is the only pointer that's guaranteed to be to an element of the
     * resulting list.
     *
     * This function assumes that one and two are the minimum elements of the
     * lists they are in, and returns a pointer to whichever is smaller.  If
     * this condition does not hold, the return value is some arbitrary pointer
     * into the doubly-linked list.
     *
     * @param one A pointer into one of the two linked lists.
     * @param two A pointer into the other of the two linked lists.
     * @return A pointer to the smallest element of the resulting list.
     */
    private static Node mergeLists(Node one, Node two) {
        /* There are four cases depending on whether the lists are null or not.
         * We consider each separately.
         */
        if (one == null && two == null) { // Both null, resulting list is null.
            return null;
        }
        else if (one != null && two == null) { // Two is null, result is one.
            return one;
        }
        else if (one == null && two != null) { // One is null, result is two.
            return two;
        }
        else { // Both non-null; actually do the splice.
            /* This is actually not as easy as it seems.  The idea is that we'll
             * have two lists that look like this:
             *
             * +----+     +----+     +----+
             * |    |--N->|one |--N->|    |
             * |    |<-P--|    |<-P--|    |
             * +----+     +----+     +----+
             *
             *
             * +----+     +----+     +----+
             * |    |--N->|two |--N->|    |
             * |    |<-P--|    |<-P--|    |
             * +----+     +----+     +----+
             *
             * And we want to relink everything to get
             *
             * +----+     +----+     +----+---+
             * |    |--N->|one |     |    |   |
             * |    |<-P--|    |     |    |<+ |
             * +----+     +----+<-\  +----+ | |
             *                  \  P        | |
             *                   N  \       N |
             * +----+     +----+  \->+----+ | |
             * |    |--N->|two |     |    | | |
             * |    |<-P--|    |     |    | | P
             * +----+     +----+     +----+ | |
             *              ^ |             | |
             *              | +-------------+ |
             *              +-----------------+
             *
             */
            Node oneNext = one.mNext; // Cache this since we're about to overwrite it.
            one.mNext = two.mNext;
            one.mNext.mPrev = one;
            two.mNext = oneNext;
            two.mNext.mPrev = two;

            /* Return a pointer to whichever's greater. */
            return one.mValue > two.mValue ? one : two;
        }
    }

    /**
     * Decreases the key of a node in the tree without doing any checking to ensure
     * that the new priority is valid.
     *
     * @param node The node whose key should be decreased.
     * @param priority The node's new priority.
     */
    private void decreaseKeyUnchecked(Node node, double priority) {
        /* First, change the node's priority. */
        node.mValue = priority;

        /* If the node no longer has a higher priority than its parent, cut it.
         * Note that this also means that if we try to run a delete operation
         * that decreases the key to -infinity, it's guaranteed to cut the node
         * from its parent.
         */
        if (node.mParent != null && node.mValue >= node.mParent.mValue)
            cutNode(node);

        /* If our new value is the new min, mark it as such.  Note that if we
         * ended up decreasing the key in a way that ties the current minimum
         * priority, this will change the min accordingly.
         */
        if (node.mValue >= mMax.mValue)
            mMax = node;
    }

    /**
     * Cuts a node from its parent.  If the parent was already marked, recursively
     * cuts that node from its parent as well.
     *
     * @param node The node to cut from its parent.
     */
    private void cutNode(Node node) {
        /* Begin by clearing the node's mark, since we just cut it. */
        node.mIsMarked = false;

        /* Base case: If the node has no parent, we're done. */
        if (node.mParent == null) return;

        /* Rewire the node's siblings around it, if it has any siblings. */
        if (node.mNext != node) { // Has siblings
            node.mNext.mPrev = node.mPrev;
            node.mPrev.mNext = node.mNext;
        }

        /* If the node is the one identified by its parent as its child,
         * we need to rewrite that pointer to point to some arbitrary other
         * child.
         */
        if (node.mParent.mChild == node) {
            /* If there are any other children, pick one of them arbitrarily. */
            if (node.mNext != node) {
                node.mParent.mChild = node.mNext;
            }
            /* Otherwise, there aren't any children left and we should clear the
             * pointer and drop the node's degree.
             */
            else {
                node.mParent.mChild = null;
            }
        }

        /* Decrease the degree of the parent, since it just lost a child. */
        --node.mParent.mDegree;

        /* Splice this tree into the root list by converting it to a singleton
         * and invoking the merge subroutine.
         */
        node.mPrev = node.mNext = node;
        mMax = mergeLists(mMax, node);

        /* Mark the parent and recursively cut it if it's already been
         * marked.
         */
        if (node.mParent.mIsMarked)
            cutNode(node.mParent);
        else
            node.mParent.mIsMarked = true;

        /* Clear the relocated node's parent; it's now a root. */
        node.mParent = null;
    }
}