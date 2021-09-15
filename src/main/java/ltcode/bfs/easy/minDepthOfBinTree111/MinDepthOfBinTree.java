package ltcode.bfs.easy.minDepthOfBinTree111;

import ltcode.ds.TreeNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 题目要求找最短，且需要遍历着去找。===> bfs
 */
public class MinDepthOfBinTree {
    public int minDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int res = 1;
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                TreeNode t = q.poll();
                if (t.left == null && t.right == null) {
                    return res;
                } else if (t.left != null && t.right != null) {
                    q.offer(t.left);
                    q.offer(t.right);
                } else if (t.left != null) {
                    q.offer(t.left);
                } else {
                    q.offer(t.right);
                }
            }
            res++;
        }
        return res;
    }
}
