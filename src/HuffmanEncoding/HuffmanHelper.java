package HuffmanEncoding;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanHelper {
    public static HashMap<Character, Integer> makeHuffmanTable(String data){
        HashMap<Character, Integer> table = new HashMap<>();
        for(char i : data.toCharArray()){
            table.put(i, table.getOrDefault(i, 0) + 1);
        }
        return table;
    }

    public static PriorityQueue<BinaryTree> getQueueFromHuffmanTable(HashMap<Character, Integer> table){
        PriorityQueue<BinaryTree> queue = new PriorityQueue<>(256, new BinaryTreeComparator());
        for(Character c : table.keySet()){
            queue.add(new BinaryTree(new Node(table.get(c), c)));
        }
        return queue;
    }

    public static BinaryTree getHuffmanCodeTree(PriorityQueue<BinaryTree> pq){
        while(true){
            BinaryTree b1 = pq.poll();
            try{
                BinaryTree b2 = pq.poll();
                Node newNode = new Node();
                newNode.addChild(b1.root());
                newNode.addChild(b2.root());
                pq.add(new BinaryTree(newNode));
            }
            catch (NullPointerException e){
                return b1;
            }
        }
    }

    public static void fillCodeTable(HashMap<Character, String> codeTable, Node node, String code, String appendix){
        if(node.isLeaf()){
            codeTable.put(node.getLetter(), code + appendix);
        }
        else{
            fillCodeTable(codeTable, node.getLeft(), code + appendix, "0");
            fillCodeTable(codeTable, node.getRight(), code + appendix, "1");
        }
    }

    public static HashMap<String, Character> getDecodeTable(String table_fn){
        HashMap<String, Character> decode_table = new HashMap<>();
        try(BufferedReader reader = new BufferedReader
                (new InputStreamReader(new FileInputStream(table_fn), StandardCharsets.UTF_8))){
            String line = reader.readLine();
            while(line != null){
                String letter_code = line.split(" : ")[0].replaceFirst("0x", "");
                Character letter = (char)Integer.parseInt(letter_code, 16);
                String code = line.split(" : ")[1];
                decode_table.put(code, letter);
                line = reader.readLine();
            }
        }
        catch (IOException e){
            System.err.println("Retrieve code-table error!");
        }
        return decode_table;
    }

    public static void exportCodeTable(HashMap<Character, String> table, String filename){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
            String hex_repro;
            for(int i = 0; i < 256; i++){
                if(table.get((char)i) != null){
                    hex_repro = String.format("0x%02X", i);
                    writer.write(hex_repro + " : " + table.get((char)i));
                    writer.newLine();
                }
            }
        }
        catch (IOException e){
            System.err.println("Write to file error!");
        }
    }
}
