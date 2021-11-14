package ltcode.binSearch.medium.seachRange34;

import java.util.Arrays;

public class SearchRange {
    public int[] searchRange(int[] nums, int target) {
        int[] res = new int[2];
        res[0] = findLeft(nums, target);
        res[1] = findRight(nums, target);
        return res;
    }

    private int findRight(int[] nums, int target) {
        int l = -1;
        int r = nums.length;
        while (l + 1 != r) {
            int mid = (l + r) / 2;
            if (nums[mid] <= target) {
                l = mid;
            } else {
                r = mid;
            }
        }
        if (l < 0 || nums[l] != target) {
            return -1;
        }
        return l;
    }

    private int findLeft(int[] nums, int target) {
        int l = -1;
        int r = nums.length;
        while (l + 1 != r) {
            int mid = (l + r) / 2;
            if (nums[mid] < target) {
                l = mid;
            } else {
                r = mid;
            }
        }
        if (r >= nums.length || nums[r] != target) {
            return -1;
        }
        return r;
    }

    public static void main(String[] args) {
        int[] in = {5,7,7,8,8,10};
        SearchRange s = new SearchRange();
        int[] res = s.searchRange(in, 11);
        System.out.println(Arrays.toString(res));
    }
}
