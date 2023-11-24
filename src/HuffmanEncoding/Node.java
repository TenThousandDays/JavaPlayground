package HuffmanEncoding;

public class Node{
    private int frequence;
    private char letter;
    private Node left;
    private Node right;

    public Node(int frequence, char letter){
        this.frequence = frequence;
        this.letter = letter;
    }

    public Node(){}

    public void addChild(Node node){
        if(left == null) left = node;
        else{
            if(left.frequence <= node.frequence) right = node;
            else{
                right = left;
                left = node;
            }
        }
        frequence += node.frequence;
    }

    public boolean isLeaf(){
        return left == null && right == null;
    }

    public int getFrequence(){
        return frequence;
    }

    public char getLetter(){
        return letter;
    }

    public Node getLeft(){
        return left;
    }

    public Node getRight(){
        return right;
    }
}
