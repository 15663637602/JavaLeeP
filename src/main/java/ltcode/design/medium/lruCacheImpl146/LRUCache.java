package ltcode.design.medium.lruCacheImpl146;

import java.util.HashMap;
import java.util.Map;

public class LRUCache {

    class DLinkedNode {
        int key;
        int value;
        DLinkedNode prev;
        DLinkedNode next;
        public DLinkedNode() {}

        public DLinkedNode(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    Map<Integer, DLinkedNode> cache;
    private int size;
    private int capacity;
    private DLinkedNode dummyHead, dummyTail;

    public LRUCache(int capacity) {
        size = 0;
        this.capacity = capacity;
        cache = new HashMap<>();
        dummyHead = new DLinkedNode();
        dummyTail = new DLinkedNode();
        dummyHead.next = dummyTail;
        dummyTail.prev = dummyHead;
    }

    public int get(int key) {
        DLinkedNode node = cache.get(key);
        if (node == null) {
            return -1;
        }
        moveNodeToHead(node);
        return node.value;
    }

    public void put(int key, int value) {
        DLinkedNode node = cache.get(key);
        if (node == null) {
            DLinkedNode newNode = new DLinkedNode(key, value);
            cache.put(key, newNode);
            addNodeToHead(newNode);
            size++;
            if (size > capacity) {
                DLinkedNode removedNode = removeTailNode();
                cache.remove(removedNode.key);
                size--;
            }
        } else {
            node.value = value;
            moveNodeToHead(node);
        }
    }

    private void moveNodeToHead(DLinkedNode node) {
        removeNode(node);
        addNodeToHead(node);
    }

    private DLinkedNode removeTailNode() {
        DLinkedNode tailNode = dummyTail.prev;
        removeNode(tailNode);
        return tailNode;
    }

    private void removeNode(DLinkedNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void addNodeToHead(DLinkedNode node) {
        node.prev = dummyHead;
        node.next = dummyHead.next;
        dummyHead.next.prev = node;
        dummyHead.next = node;
    }
}
