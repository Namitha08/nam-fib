package edu.ufl.ads;
/**
 * In order for all of the Fibonacci heap operations to complete in O(1),
 * clients need to have O(1) access to any element in the heap.  We make
 * this work by having each insertion operation produce a handle to the
 * node in the tree.  In actuality, this handle is the node itself, but
 * we guard against external modification by marking the internal fields
 * private.
 */
public class Entry<T> {
        int     mDegree = 0;       // Number of children
        boolean mIsMarked = false; // Whether this node is marked

        Entry<T> mNext;   // Next and previous elements in the list
        Entry<T> mPrev;

        Entry<T> mParent; // Parent in the tree, if any.

        Entry<T> mChild;  // Child node, if any.

        private T  mElem;     // Element being stored here
        double mPriority; // Its priority

        /**
         * Returns the element represented by this heap entry.
         *
         * @return The element represented by this heap entry.
         */
        public T getValue() {
            return mElem;
        }
        /**
         * Sets the element associated with this heap entry.
         *
         * @param value The element to associate with this heap entry.
         */
        public void setValue(T value) {
            mElem = value;
        }

        /**
         * Returns the priority of this element.
         *
         * @return The priority of this element.
         */
        public double getPriority() {
            return mPriority;
        }

        /**
         * Constructs a new Entry that holds the given element with the indicated 
         * priority.
         *
         * @param elem The element stored in this node.
         * @param priority The priority of this element.
         */
        public Entry(T elem, double priority) {
            mNext = mPrev = this;
            mElem = elem;
            mPriority = priority;
        }
    }