package ltcode.binSearch.easy.binSearch704;

public class BinSearch {
    public int search(int[] nums, int target) {
        int l = -1;
        int r = nums.length;
        while (l + 1 != r) {
            int mid = (l + r) / 2;
            if (target < nums[mid]) {
                r = mid;
            } else if (target > nums[mid]) {
                l = mid;
            } else {
                return mid;
            }
        }
        return -1;
    }
}
