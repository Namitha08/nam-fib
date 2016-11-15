package edu.ufl.ads;

public class Node {
    int degree = 0;
    boolean childCut = false; // isMarked for child cut
    Node next;
    Node prev;
    Node parent;
    Node child;
    String name;
    double value;

    @Override
    public String toString() {
        return name + ":" + value;
    }

    public double getPriority() {
        return value;
    }

    public Node(double priority) {
        next = prev = this;
        value = priority;
    }
}