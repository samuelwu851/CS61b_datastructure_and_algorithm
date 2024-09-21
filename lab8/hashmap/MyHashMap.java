package hashmap;

import java.util.*;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    private double loadFactor = 0.75;
    private int tableSize = 16;
    private int nodeNum = 0;
    private Collection<Node>[] buckets; // the bucket's objects
    private Set<K> keySet = new HashSet<>();


    /**
     * Constructors
     */
    public MyHashMap() {
        buckets = createTable(tableSize);
    }

    public MyHashMap(int initialSize) {
        tableSize = initialSize;
        buckets = createTable(tableSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        tableSize = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(tableSize);
    }


    @Override
    public void clear() {
        buckets = createTable(tableSize);
        nodeNum = 0;
        keySet = new HashSet<>();
    }

    @Override
    public boolean containsKey(K key) {
        int tableIndex = getTableIndex(key.hashCode());
        if (buckets[tableIndex] != null) {
            for (Node oldNode : buckets[tableIndex]) {
                if (oldNode.key.equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int tableIndex = getTableIndex(key.hashCode());
        if (buckets[tableIndex] != null) {
            for (Node oldNode : buckets[tableIndex]) {
                if (oldNode.key.equals(key)) {
                    return oldNode.value;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        return nodeNum;
    }

    @Override
    public void put(K key, V value) {
        Node node = new Node(key, value);
        int tableIndex = getTableIndex(key.hashCode());
        // if it doesn't exist, we create it
        if (buckets[tableIndex] == null) {
            buckets[tableIndex] = createBucket();
        }
        // replace old value if exists
        for (Node oldNode : buckets[tableIndex]) {
            if (oldNode.key.equals(key)) {
                oldNode.value = value;
                return;
            }
        }
        // if no oldNode exists, add it to the end
        buckets[tableIndex].add(node);
        keySet.add(key);
        nodeNum++;
        if ((double) (nodeNum / tableSize) >= loadFactor) {
            resize();
        }
    }

    private void resize() {
        LinkedList<Node> oldNodes = new LinkedList<>();
        for (int i = 0; i < tableSize; i++) {
            if (buckets[i] != null) {
                for (Node node : buckets[i]) {
                    oldNodes.add(node);
                }
            }
        }
        tableSize *= 2;
        this.clear();
        for (Node node : oldNodes) {
            this.put(node.key, node.value);
        }
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        if (containsKey(key)) {
            keySet.remove(key);
            nodeNum--;
            int index = getTableIndex(key.hashCode());
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    V value = node.value;
                    buckets[index].remove(node);
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        if (containsKey(key)) {
            keySet.remove(key);
            nodeNum--;
            int index = getTableIndex(key.hashCode());
            for (Node node : buckets[index]) {
                if (node.key.equals(key) || node.value.equals(value)) {
                    buckets[index].remove(node);
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new IteratorOfKey();
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    private int getTableIndex(int hashCode) {
        return Math.floorMod(hashCode, tableSize);
    }

    private class IteratorOfKey implements Iterator<K> {

        LinkedList<K> keysList = createKeyList();
        int keyNum = 0;

        private LinkedList<K> createKeyList() {
            LinkedList<K> keyList = new LinkedList<>();
            for (int i = 0; i < tableSize; i++) {
                if (buckets[i] != null) {
                    for (Node node : buckets[i]) {
                        keyList.add(node.key);
                    }
                }
            }
            return keyList;
        }


        @Override
        public boolean hasNext() {
            return keyNum != nodeNum;
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                return keysList.remove();
            }
        }
    }
}

