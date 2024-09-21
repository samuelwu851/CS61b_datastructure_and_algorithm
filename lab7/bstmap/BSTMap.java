package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;


public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private Node root;
    private HashSet<K> keySet = new HashSet<>();

    private class Node {
        private K key;
        private V val;
        private Node left, right;
        private int size;

        public Node(K key, V val, int size) {
            this.key = key;
            this.val = val;
            this.size = size;
        }
    }

    public BSTMap() {
    }


    public void clear() {
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null)
            throw new IllegalArgumentException("calls put() with a null key");
        return containsKey(root, key) != null;
    }

    private K containsKey(Node x, K key){
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp < 0) return containsKey(x.left, key);
        if (cmp > 0) return containsKey(x.right, key);
        return x.key;
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(Node x, K key) {
        if (key == null)
            throw new IllegalArgumentException("calls put() with a null key");
        if (x == null)
            return null;
        int cmp = key.compareTo(x.key);
        if (cmp < 0) return get(x.left, key);
        else if (cmp > 0) return get(x.right, key);
        else return x.val;
    }

    private boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return size(root);
    }

    private int size(Node root) {
        if (root == null) return 0;
        else return root.size;
    }

    @Override
    public void put(K key, V value) {
        if (key == null)
            throw new IllegalArgumentException("calls put() with a null key");
        if (value == null) {
            remove(key);
            return;
        }
        root = put(root, key, value);
        keySet.add(key);
    }

    public Set<K> keySet() {
        return keySet;
    }

    private Node put(Node x, K key, V val) {
        //if this is the first node in mao
        if (x == null)
            return new Node(key, val, 1);
        int cmp = key.compareTo(x.key);
        if (cmp < 0) x.left = put(x.left, key, val);
        else if (cmp > 0) x.right = put(x.right, key, val);
        else x.val = val;
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    @Override
    public V remove(K key) {
        if (key == null)
            throw new IllegalArgumentException("calls put() with a null key");
        V removeValue = get(key);
        root = remove(root, key);
        keySet.remove(key);
        return removeValue;
    }

    @Override
    public V remove(K key, V value) {
        if (key == null) throw new IllegalArgumentException("calls remove() with a null key");
        root = remove(root, key);
        keySet.remove(key);
        return value;
    }

    /**
     * NOTE!!!
     * At line 138, 139
     * you can't just use
     *        if (cmp < 0) return = remove(x.left, key);
     *        else if (cmp > 0) return = remove(x.right, key);
     * as the given sample did,
     * we would lost the root and only the subtree left!!!
     */

    private Node remove(Node x, K key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp < 0) x.left = remove(x.left, key);
        else if (cmp > 0) x.right = remove(x.right, key);
        else {
            // with only one child, point my parent to my child
            if (x.right == null) return x.left;
            if (x.left == null) return x.right;
            // with two children, then we would need a new root node
            Node temp = x;
            //this x will be the new root
            x = min(temp.right);
            x.right = deleteMin(temp.right);
            x.left = temp.left;
        }
        x.size = size(x.left) + size(x.right) + 1;
        return x;
    }

    private K min() {
        if (isEmpty()) throw new NoSuchElementException("calls min() with empty symbol table");
        return min(root).key;
    }

    private Node min(Node x) {
        if (x.left == null) return x;
        return min(x.left);
    }


    private void deleteMin() {
        if (isEmpty()) throw new NoSuchElementException("Symbol table underflow");
        root = deleteMin(root);
    }


    // set the right child of min(which is gonna be the new root)
    // to be the left child of its parent
    private Node deleteMin(Node x) {
        if (x.left == null) return x.right;
        x.left = deleteMin(x.left);
        x.size = size(x.left) + size(x.right) + 1;
        return x;
    }

    @Override
    public Iterator<K> iterator() {
        return new IteratorOfKey(root);
    }

    private class IteratorOfKey implements Iterator<K> {
        private Node node;

        public IteratorOfKey(Node node) {
            this.node = node;
        }

        public boolean hasNext() {
            return node != null;
        }

        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                K minKey = min(node).key;
                node = BSTMap.this.remove(node, minKey);
                return minKey;
            }
        }
    }


    public void printInOrder() {
        printInorder(root);
    }

    private void printInorder(Node x) {
        if (x == null) return;
        printInorder(x.left);
        System.out.println("key: " + x.key + "value: " + x.val);
        System.out.println(x.right);
    }
}
