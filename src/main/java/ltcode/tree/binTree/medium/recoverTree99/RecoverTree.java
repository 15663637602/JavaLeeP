package ltcode.tree.binTree.medium.recoverTree99;

import ds.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class RecoverTree {
    public void recoverTree(TreeNode root) {
        List<Integer> arr = new ArrayList<>();
        inOrder(root, arr);
        int firstIdx = -1;
        int secondIdx = -1;
        for (int i = 0; i < arr.size() - 1; i++) {
            if (arr.get(i) > arr.get(i + 1)) {
                if (firstIdx == -1) {
                    firstIdx = i;
                }
                secondIdx = i + 1;
            }
        }
        int firstVal = arr.get(firstIdx);
        int secondVal = arr.get(secondIdx);
        inOrderChg(root, firstVal, secondVal);
    }

    private void inOrderChg(TreeNode root, int firstVal, int secondVal) {
        if (root == null) {
            return;
        }
        inOrderChg(root.left, firstVal, secondVal);
        if (root.val == firstVal) {
            root.val = secondVal;
        } else if (root.val == secondVal) {
            root.val = firstVal;
        }
        inOrderChg(root.right, firstVal, secondVal);
    }

    private void inOrder(TreeNode root, List<Integer> arr) {
        if (root == null) {
            return;
        }
        inOrder(root.left, arr);
        arr.add(root.val);
        inOrder(root.right, arr);
    }


    TreeNode prev = null;
    TreeNode first = null;
    TreeNode second = null;
    public void recoverTree2(TreeNode root) {
        inTraverse(root);
        int tmp = first.val;
        first.val = second.val;
        second.val = tmp;
    }

    private void inTraverse(TreeNode root) {
        if (root == null) {
            return;
        }
        inTraverse(root.left);
        if (prev != null) {
            if (prev.val > root.val) {
                if (first == null) {
                    first = prev;
                }
                second = root;
            }
        }
        prev = root;
        inTraverse(root.right);
    }
}
