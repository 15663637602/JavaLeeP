package ltcode.tree.binTree.travese;

import ds.TreeNode;
import ds.TreeUtil;

import java.util.ArrayDeque;
import java.util.Deque;

public class BinTreeNonRecursiveTraverse {
    public static void pre(TreeNode root) {
        if (root != null) {
            Deque<TreeNode> st = new ArrayDeque<>();
            st.push(root);
            while (!st.isEmpty()) {
                TreeNode pop = st.pop();
                System.out.print(pop.val + " ");
                if (pop.right != null) {
                    st.push(pop.right);
                }
                if (pop.left != null) {
                    st.push(pop.left);
                }
            }
        }
        System.out.println();
    }

    public static void in(TreeNode root) {
        if (root != null) {
            Deque<TreeNode> st = new ArrayDeque<>();
            TreeNode cur = root;
            while (!st.isEmpty() || cur != null) {
                if (cur != null) {
                    st.push(cur);
                    cur = cur.left;
                } else {
                    TreeNode pop = st.pop();
                    System.out.print(pop.val + " ");
                    cur = pop.right;
                }
            }
            System.out.println();
        }
    }

    public static void in2(TreeNode head) {
        if (head != null) {
            Deque<TreeNode> st = new ArrayDeque<>();
            st.push(head);
            while (!st.isEmpty()) {
                if (head.left != null) {
                    head = head.left;
                    st.push(head);
                } else {
                    head = st.pop();
                    System.out.print(head.val + " ");
                    if (head.right != null) {
                        head = head.right;
                        st.push(head);
                    }
                }
            }
        }
    }

    public static void post(TreeNode head) {
        if (head != null) {
            Deque<TreeNode> st = new ArrayDeque<>();
            Deque<TreeNode> ret = new ArrayDeque<>();
            st.push(head);
            while (!st.isEmpty()) {
                TreeNode pop = st.pop();
                ret.push(pop);
                if (pop.left != null) {
                    st.push(pop.left);
                }
                if (pop.right != null) {
                    st.push(pop.right);
                }
            }
            while (!ret.isEmpty()) {
                System.out.print(ret.pop().val + " ");
            }
            System.out.println();
        }
    }

    /**
     * 将当前节点currNode的前驱节点[下个左孩子]precursorNode的最右子节点lastMostNode的右孩子指向当前节点currNode
     * @param root
     */
    public static void morrisInOrderTraverse(TreeNode root) {
        TreeNode currNode = root;
        while (currNode != null) {
            if (currNode.left == null) {
                // 如果没有左子，直接走右子
                System.out.println(currNode.val);
                currNode = currNode.right;
            } else {
                TreeNode precursorNode = currNode.left;
                // 遍历到最右下
                while (precursorNode.right != null && precursorNode.right != currNode) {
                    precursorNode = precursorNode.right;
                }
                if (precursorNode.right == currNode) {
                    // 意味着前驱节点的右节点已经被设置，该次遍历为回溯
                    // 左边已经搞定，接下来需要处理右边
//                    precursorNode.right = null;
                    System.out.println(currNode.val + " 线索回归打印");
                    currNode = currNode.right;
                } else {
                    // 第一次访问该前驱节点，设置线索，即右节点指向当前节点
                    precursorNode.right = currNode;
                    System.out.printf("%s 设置线索(%s)%n", currNode.val, precursorNode.val);
                    currNode = currNode.left;
                }
            }
        }
    }


    public static void main(String[] args) {
        TreeNode tree3 = TreeUtil.giveMeTreeThree();
        morrisInOrderTraverse(tree3);
    }
}
