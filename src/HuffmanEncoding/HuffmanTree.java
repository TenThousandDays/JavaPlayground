package HuffmanEncoding;

import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanTree {

    private final String message;
    private final HashMap<Character, Integer> freqTable;
    private final BinaryTree huffmanTree;
    private final HashMap<Character, String> codeTable = new HashMap<>();

    public HuffmanTree(String msg){
        message = msg;
        freqTable = createFreqTable();
        huffmanTree = createHuffmanTree();
        fillCodeTable(huffmanTree.root(), "", "");
    }

    private HashMap<Character, Integer> createFreqTable(){
        HashMap<Character, Integer> table = new HashMap<>();
        char letter;
        for(int i = 0; i < message.length(); i++){
            letter = message.charAt(i);
            table.put(letter, table.getOrDefault(letter, 0) + 1);
        }
        return table;
    }

    private BinaryTree createHuffmanTree(){
        PriorityQueue<BinaryTree> pq = new PriorityQueue<>(new BinaryTreeComparator());
        for(Character c : freqTable.keySet()){
            pq.add(new BinaryTree(new Node(freqTable.get(c), c)));
        }
        BinaryTree result;
        while(true){
            result = pq.poll();
            try{
                BinaryTree temp = pq.poll();
                Node newNode = new Node();
                newNode.addChild(result.root());
                newNode.addChild(temp.root());
                pq.add(new BinaryTree(newNode));
            }
            catch (NullPointerException e){
                return result;
            }
        }
    }

    private void fillCodeTable(Node node, String code, String appendix){
        if(node.isLeaf()){
            codeTable.put(node.getLetter(), code + appendix);
        }
        else{
            fillCodeTable(node.getLeft(), code + appendix, "0");
            fillCodeTable(node.getRight(), code + appendix, "1");
        }
    }

    public String getMessage(){
        return message;
    }

    public HashMap<Character, Integer> getFreqTable(){
        return freqTable;
    }

    public BinaryTree getHuffmanTree(){
        return huffmanTree;
    }

    public HashMap<Character, String> getCodeTable(){
        return codeTable;
    }

}
