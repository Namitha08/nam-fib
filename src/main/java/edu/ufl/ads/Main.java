package edu.ufl.ads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 *
 */
public class Main {

    public static final String REG_EX = "#([^\\s]+) (\\d+)";
    private static final Pattern PATTERN = Pattern.compile(REG_EX);

    public static void main(String[] args) {

        FibonacciHeap fibonacciHeap = new FibonacciHeap();
        Map<String, Node> hashMap = new HashMap<String, Node>();
        Map<Node, String> reverseHash = new HashMap<Node, String>();
//
//        Node enqueue = fibonacciHeap.enqueue(21);
//        hashMap.put("node1", enqueue);
//        hashMap.put("node2", fibonacciHeap.enqueue(12));
//        hashMap.put("node3", fibonacciHeap.enqueue(111));
//        hashMap.put("node4", fibonacciHeap.enqueue(14));
//        hashMap.put("node5", fibonacciHeap.enqueue(105));
//
//        fibonacciHeap.decreaseKey(hashMap.get("node4"), 123);

        try {
            File file = new File("/Users/sn1/github/fibonacci-heap/src/main/resources/sample.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.startsWith("#")){
                    Matcher m = PATTERN.matcher(line);
                    if(m.find()){
                        String hashTagName = m.group(1);
                        String hashTagStringValue = m.group(2);

                        double value = Double.parseDouble(hashTagStringValue);

                        //try to find if node is already present
                        Node node = hashMap.get(hashTagName);

                        if(node == null){ // if node is null insert into fibonacciHeap and hashMap
                            Node newNode = fibonacciHeap.enqueue(value);
                            newNode.name = hashTagName;
                            hashMap.put(hashTagName, newNode);
                            reverseHash.put(newNode, hashTagName);
                        } else { //if node is already exists increase node value
                            double existingValue = node.mValue;
                            fibonacciHeap.decreaseKey(node, (existingValue + value));
                        }
                    }
                } else if(line.equals("stop")){
                    System.exit(1);
                } else {
                    System.out.println("----------------------------");
                    Map<String, Double> buffer = new TreeMap<String, Double>(); //sort decreasing order
                    Long aLong = Long.parseLong(line);
                    for(Long i = 0L; i < aLong; i++) {
                        Node node = fibonacciHeap.dequeueMax();
                        String hashTagName = reverseHash.get(node);
                        System.out.println(hashTagName + " -->"+ node.getPriority());
                        buffer.put(hashTagName, node.getPriority());
                    }

                    for (String key : buffer.keySet()){
                        Node enqueuedNode = fibonacciHeap.enqueue(buffer.get(key));
                        hashMap.put(key, enqueuedNode);
                        reverseHash.put(enqueuedNode, key);
                    }
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
