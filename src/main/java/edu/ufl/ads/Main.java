package edu.ufl.ads;

/***
 *
 */
public class Main {
    public static void main(String[] args) {
        FibonacciHeap<String> fibonacciHeap = new FibonacciHeap<String>();
        fibonacciHeap.enqueue("Str1", 105);
        fibonacciHeap.enqueue("str2", 21);
        fibonacciHeap.enqueue("str4", 12);
        fibonacciHeap.enqueue("str8", 11);
        fibonacciHeap.enqueue("str6", 134);

        System.out.printf(fibonacciHeap.dequeueMin().getValue()+"");
    }
}
