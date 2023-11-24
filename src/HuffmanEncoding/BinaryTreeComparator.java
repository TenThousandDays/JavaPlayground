package HuffmanEncoding;

import java.util.Comparator;

public class BinaryTreeComparator implements Comparator<BinaryTree> {
    @Override
    public int compare(BinaryTree m1, BinaryTree m2) {
        if(m1.getFrequence() < m2.getFrequence()) return -1;
        else if(m1.getFrequence() > m2.getFrequence()) return 1;
        return 0;
    }
}