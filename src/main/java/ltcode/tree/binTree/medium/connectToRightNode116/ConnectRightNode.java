package ltcode.tree.binTree.medium.connectToRightNode116;

class Node {
    public int val;
    public Node left;
    public Node right;
    public Node next;

    public Node() {
    }

    public Node(int _val) {
        val = _val;
    }

    public Node(int _val, Node _left, Node _right, Node _next) {
        val = _val;
        left = _left;
        right = _right;
        next = _next;
    }
};

public class ConnectRightNode {
    public Node connect(Node root) {
        connect(root.left, root.right);
        return root;
    }

    private void connect(Node left, Node right) {
        if (left == null || right == null) {
            return;
        }
        left.next = right;
        connect(left.left, left.right);
        connect(left.right, right.left);
        connect(right.left, right.right);
    }
}
