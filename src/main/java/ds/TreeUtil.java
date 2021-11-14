package ds;

public class TreeUtil {
    /**
     *                  -10
     *          9                 20
     *                      15          7
     * @return
     */
    public static TreeNode giveMeTreeOne() {
        TreeNode head = new TreeNode(-10);

        TreeNode hL = new TreeNode(9);
        TreeNode hR = new TreeNode(20);
        head.left = hL;
        head.right = hR;

        TreeNode hRL = new TreeNode(15);
        TreeNode hRR = new TreeNode(7);
        hR.left = hRL;
        hR.right = hRR;

        return head;
    }


    /**
     *                  -10
     *          9                 20
     *                      15          7
     *                  17
     * @return
     */
    public static TreeNode giveMeTreeTwo() {
        TreeNode head = new TreeNode(-10);

        TreeNode hL = new TreeNode(9);
        TreeNode hR = new TreeNode(20);
        head.left = hL;
        head.right = hR;

        TreeNode hRL = new TreeNode(15);
        TreeNode hRR = new TreeNode(7);
        hR.left = hRL;
        hR.right = hRR;

        TreeNode hRLL = new TreeNode(17);
        hRL.left = hRLL;

        return head;
    }


    /**
     *                  -10
     *          9                 20
     *                      15          7
     *                  17     19
     * @return
     */
    public static TreeNode giveMeTreeThree() {
        TreeNode head = new TreeNode(-10);

        TreeNode hL = new TreeNode(9);
        TreeNode hR = new TreeNode(20);
        head.left = hL;
        head.right = hR;

        TreeNode hRL = new TreeNode(15);
        TreeNode hRR = new TreeNode(7);
        hR.left = hRL;
        hR.right = hRR;

        TreeNode hRLL = new TreeNode(17);
        hRL.left = hRLL;

        TreeNode hRLR = new TreeNode(19);
        hRL.right = hRLR;

        return head;
    }
}
