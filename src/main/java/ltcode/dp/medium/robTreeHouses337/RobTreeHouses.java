package ltcode.dp.medium.robTreeHouses337;

import ds.TreeNode;

import java.util.HashMap;
import java.util.Map;

public class RobTreeHouses {
    public int robCumbersome(TreeNode root) {
        if (root == null) return 0;
        int money = root.val;
        if (root.left != null) {
            money += (robCumbersome(root.left.left) + robCumbersome(root.left.right));
        }
        if (root.right != null) {
            money += (robCumbersome(root.right.left) + robCumbersome(root.right.right));
        }
        return Math.max(money, robCumbersome(root.left) + robCumbersome(root.right));
    }

    public int robTuned(TreeNode root) {
        Map<TreeNode, Integer> cache = new HashMap<>();
        return robTunedHelper(root, cache);
    }

    private int robTunedHelper(TreeNode root, Map<TreeNode, Integer> cache) {
        if (root == null) return 0;
        if (cache.containsKey(root)) return cache.get(root);
        int money = root.val;

        if (root.left != null)
            money += (robTunedHelper(root.left.left, cache) + robTunedHelper(root.left.right, cache));
        if (root.right != null)
            money += (robTunedHelper(root.right.left, cache) + robTunedHelper(root.right.right, cache));

        int res = Math.max(money, robTunedHelper(root.left, cache) + robTunedHelper(root.right, cache));
        cache.put(root, res);
        return res;
    }

    /*
    每个节点可选择偷或者不偷两种状态，根据题目意思，相连节点不能一起偷

    当前节点选择偷时，那么两个孩子节点就不能选择偷了
    当前节点选择不偷时，两个孩子节点只需要拿最多的钱出来就行(两个孩子节点偷不偷没关系)
    我们使用一个大小为 2 的数组来表示 int[] res = new int[2] 0 代表不偷，1 代表偷
    任何一个节点能偷到的最大钱的状态可以定义为

    当前节点选择不偷：当前节点能偷到的最大钱数 = 左孩子能偷到的钱 + 右孩子能偷到的钱
    当前节点选择偷：当前节点能偷到的最大钱数 = 左孩子选择自己不偷时能得到的钱 + 右孩子选择不偷时能得到的钱 + 当前节点的钱数
    表示为公式如下 0不偷 1偷

    root[0] = Math.max(rob(root.left)[0], rob(root.left)[1]) + Math.max(rob(root.right)[0], rob(root.right)[1])
    root[1] = rob(root.left)[0] + rob(root.right)[0] + root.val;
     */

    public int rob(TreeNode root) {
        int[] result = robHelper(root);
        return Math.max(result[0], result[1]);
    }

    private int[] robHelper(TreeNode root) {
        if (root == null) return new int[2];
        int[] ret = new int[2];
        int[] left = robHelper(root.left);
        int[] right = robHelper(root.right);

        ret[0] = Math.max(left[0], left[1]) + Math.max(right[0], right[1]);
        ret[1] = root.val + left[0] + right[0];
        return ret;
    }
}
