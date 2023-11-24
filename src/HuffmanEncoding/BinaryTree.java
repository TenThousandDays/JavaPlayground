package HuffmanEncoding;

public record BinaryTree(Node root) {

    int getFrequence() {
        return root.getFrequence();
    }

}