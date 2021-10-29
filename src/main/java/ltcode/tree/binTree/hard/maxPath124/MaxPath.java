package ltcode.tree.binTree.hard.maxPath124;

import ds.TreeNode;
import ds.TreeUtil;

/**
 *   a
 * b   c
 * 有三种情况
 * 1. b->a->c => node.val + node.left.val + node.right.val / node.val + node.left.val [node.right.val是负数] / node.val + node.right.val [node.left.val是负数] / node.val
 * 2. b->a->a的父 => a的父 + a + b
 * 3. c->a->a的父 => a的父 + a + c
 */
public class MaxPath {
    int ans = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        if (root == null) {
            return 0;
        }
        traverse(root);
        return ans;
    }

    private int traverse(TreeNode root) {
        if (root == null) {
            return 0;
        }
        // 节点可能有负值，如果左边/右边的最大值为负数，那还不如不要
        int left = Math.max(0, traverse(root.left));
        int right = Math.max(0, traverse(root.right));
        // 后序遍历
        //left->当前节点->right 作为路径与已经计算过历史最大值做比较
        ans = Math.max(ans, left + right + root.val);
        // 返回经过当前节点的 [单边] 最大分支 给当前节点的父节点 供它计算使用
        return Math.max(left, right) + root.val;
    }

    public static void main(String[] args) {
        TreeNode root = TreeUtil.giveMeTreeThree();
        MaxPath m = new MaxPath();
        int ret = m.maxPathSum(root);
        System.out.println(ret);
    }

}
