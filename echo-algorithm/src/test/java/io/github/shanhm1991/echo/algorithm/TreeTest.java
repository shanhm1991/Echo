package io.github.shanhm1991.echo.algorithm;

import io.github.shanhm1991.echo.algorithm.tree.AvlTreeSet;
import io.github.shanhm1991.echo.algorithm.tree.BinaryTreeSet;
import io.github.shanhm1991.echo.algorithm.tree.RBTreeSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.TreeSet;

/**
 *
 * @author shanhm1991@163.com
 *
 */
public class TreeTest {
    
    @Test
    public void testBinaryTreeSet() {
        TreeSet<Integer> treeSet = new TreeSet<>(); 
        BinaryTreeSet<Integer> binaryTreeSet = new BinaryTreeSet<>();

        int[] array = {10, 11, 9, 5, 2, 1, 8, 6, 4, 3};
        for (int val : array) {
            treeSet.add(val);
            binaryTreeSet.add(val);
        }
        Assertions.assertArrayEquals(treeSet.toArray(), binaryTreeSet.toList().toArray());

        treeSet.remove(5);
        binaryTreeSet.remove(5);
        Assertions.assertArrayEquals(treeSet.toArray(), binaryTreeSet.toList().toArray());
    }

    @Test
    public void testAvlTreeSet() {
        TreeSet<Integer> treeSet = new TreeSet<>();
        AvlTreeSet<Integer> avlTreeSet = new AvlTreeSet<>();

        int[] array = {10, 11, 9, 5, 2, 1, 6, 8};
        for (int val : array) {
            treeSet.add(val);
            avlTreeSet.add(val);
        }
        Assertions.assertArrayEquals(treeSet.toArray(), avlTreeSet.toList().toArray());

        treeSet.remove(1);
        avlTreeSet.remove(1);
        Assertions.assertArrayEquals(treeSet.toArray(), avlTreeSet.toList().toArray());
    }

    @Test
    public void testRBTreeSet() {
        TreeSet<Integer> treeSet = new TreeSet<>();
        RBTreeSet<Integer> rbTreeSet = new RBTreeSet<>();

        SecureRandom random = new SecureRandom();
        for(int i = 0; i < 20; i++) {
            int val = random.nextInt(100);
            treeSet.add(val);
            rbTreeSet.add(val);
        }
        Assertions.assertArrayEquals(treeSet.toArray(), rbTreeSet.toList().toArray());
    }
    
}
