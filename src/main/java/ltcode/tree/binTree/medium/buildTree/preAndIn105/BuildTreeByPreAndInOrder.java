package ltcode.tree.binTree.medium.buildTree.preAndIn105;

import ds.TreeNode;

public class BuildTreeByPreAndInOrder {
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        return buildTree(preorder, 0, preorder.length - 1, inorder, 0, inorder.length - 1);
    }

    private TreeNode buildTree(int[] preorder, int preStart, int preEnd, int[] inorder, int inStart, int inEnd) {
        if (preStart == preEnd || inStart == inEnd) {
            return new TreeNode(preorder[preStart]);
        }
        if (preStart > preEnd || inStart > inEnd) {
            return null;
        }
        int rootVal = preorder[preStart];
        TreeNode root = new TreeNode(rootVal);

        int rootIdxOfInOrder = -1;

        for (int i = inStart; i <= inEnd; i++) {
            if (inorder[i] == rootVal) {
                rootIdxOfInOrder = i;
                break;
            }
        }
        int leftLen = rootIdxOfInOrder - inStart;

        root.left = buildTree(preorder, preStart + 1, preStart + leftLen, inorder, inStart, rootIdxOfInOrder - 1);
        root.right = buildTree(preorder, preStart + leftLen + 1, preEnd, inorder, rootIdxOfInOrder + 1, inEnd);
        return root;
    }

    public static void main(String[] args) {
        int[] preorder = {1,2};
        int[] inorder = {2,1};
        BuildTreeByPreAndInOrder m = new BuildTreeByPreAndInOrder();
        TreeNode ret = m.buildTree(preorder, inorder);
        System.out.println(ret);
    }
}
