package ltcode.dp.medium.rob198;

/*
    i代表有i个house可偷的话，最多能偷多少钱
    dp[i] = max(dp[i - 2] + nums[i], dp[i - 1])
    dp[0] = nums[0]
    dp[1] = max(nums[0], nums[1])
*/



public class Rob {
    public int rob(int[] nums) {
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
        Rob m = new Rob();
        int res = m.rob(new int[]{2, 7, 9, 3, 1});
        System.out.println(res);
    }
}
