package edu.ufl.ads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 *
 */
public class Main {

    public static final String REG_EX = "#([^\\s]+)\\s+(\\d+)";
    private static final Pattern PATTERN = Pattern.compile(REG_EX);

    public static void main(String[] args) {

        MaxFibonacciHeap maxFibonacciHeap = new MaxFibonacciHeap();
        Map<String, Node> hashMap = new HashMap<String, Node>();
        Map<Node, String> reverseHash = new HashMap<Node, String>();

        if(args.length != 1){
            System.out.println("Please provide absolute path of the file!!");
            System.exit(1);
        }

//        Node enqueue = maxFibonacciHeap.enqueue(21);
//        hashMap.put("node1", enqueue);
//        hashMap.put("node2", maxFibonacciHeap.enqueue(12));
//        hashMap.put("node3", maxFibonacciHeap.enqueue(111));
//        hashMap.put("node4", maxFibonacciHeap.enqueue(14));
//        hashMap.put("node5", maxFibonacciHeap.enqueue(105));
//
//        maxFibonacciHeap.increaseKey(hashMap.get("node4"), 123);

        try {
            File file = new File(args[0]);
            if(!file.exists()){
                System.out.println("File not found");
                System.exit(1);
            }
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

                        if(node == null){ // if node is null insert into maxFibonacciHeap and hashMap
                            Node newNode = maxFibonacciHeap.enqueue(value);
                            newNode.name = hashTagName;
                            hashMap.put(hashTagName, newNode);
                            reverseHash.put(newNode, hashTagName);
                        } else { //if node is already exists increase node value
                            double existingValue = node.mValue;
                            maxFibonacciHeap.increaseKey(node, (existingValue + value));
                        }
                    }
                } else if(line.equalsIgnoreCase("stop")){
                    System.exit(1);
                } else {
                    Map<String, Double> buffer = new TreeMap<String, Double>();
                    Long aLong = Long.parseLong(line);
                    boolean oneExists = false;
                    for(Long i = 0L; i < aLong; i++) {
                        Node node = maxFibonacciHeap.dequeueMax();
                        String hashTagName = reverseHash.get(node);
                        if(oneExists)
                            System.out.print(",");
                        System.out.print(hashTagName);
                        oneExists = true;
                        buffer.put(hashTagName, node.getPriority());
                    }
                    System.out.println("");
                    for (String key : buffer.keySet()){
                        Node enqueuedNode = maxFibonacciHeap.enqueue(buffer.get(key));
                        hashMap.put(key, enqueuedNode);
                        reverseHash.put(enqueuedNode, key);
                    }
                }
            }
            fileReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
