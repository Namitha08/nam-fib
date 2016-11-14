package edu.ufl.ads;
/**
 * In order for all of the Fibonacci heap operations to complete in O(1),
 * clients need to have O(1) access to any element in the heap.  We make
 * this work by having each insertion operation produce a handle to the
 * node in the tree.  In actuality, this handle is the node itself, but
 * we guard against external modification by marking the internal fields
 * private.
 */
public class Node {
        int mDegree = 0;       // Number of children
        boolean mIsMarked = false; // Whether this node is marked

        Node mNext;   // Next and previous elements in the list
        Node mPrev;

        Node mParent; // Parent in the tree, if any.

        Node mChild;  // Child node, if any.
        String name;

    @Override
    public String toString() {
        return
                name + ":" + mValue
                ;
    }

    double mValue; // Its priority

        /**
         * Returns the priority of this element.
         *
         * @return The priority of this element.
         */
        public double getPriority() {
            return mValue;
        }

        /**
         * Constructs a new Node that holds the given element with the indicated
         * priority.
         *
         * @param priority The priority of this element.
         */
        public Node(double priority) {
            mNext = mPrev = this;
            mValue = priority;
        }
    }