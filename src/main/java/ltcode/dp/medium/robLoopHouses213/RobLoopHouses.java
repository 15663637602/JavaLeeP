package ltcode.dp.medium.robLoopHouses213;

import java.util.Arrays;

/**
 * 环状排列意味着第一个房子和最后一个房子中只能选择一个偷窃，因此可以把此环状排列房间问题约化为两个单排排列房间子问题：
 *
 * 在不偷窃第一个房子的情况下（即 nums[1:]nums[1:]），最大金额是 p_1p
 * 在不偷窃最后一个房子的情况下（即 nums[:n-1]nums[:n−1]），最大金额是 p_2p
 * 综合偷窃最大金额： 为以上两种情况的较大值，即 max(p1,p2)max(p1,p2) 。
 */
public class RobLoopHouses {
    public int rob(int[] nums) {
        if (nums.length == 1) {
            return nums[0];
        }
        return Math.max(helper(Arrays.copyOfRange(nums, 0, nums.length - 1)), helper(Arrays.copyOfRange(nums, 1, nums.length)));
    }

    public int helper(int[] nums) {
        if (nums.length == 1) {
            return nums[0];
        }
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);
        for (int i = 2; i < nums.length; i++) {
            dp[i] = Math.max(nums[i] + dp[i - 2], dp[i - 1]);
        }
        return dp[nums.length - 1];
    }

    public static void main(String[] args) {
        RobLoopHouses m = new RobLoopHouses();
        int res = m.rob(new int[]{1,2,3,1});
        System.out.println(res);
    }
}
