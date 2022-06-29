package ltcode.array.doublePointer.easy.removeDuplicates26;

public class RemoveDuplicates {
    public int removeDuplicates(int[] nums) {
        int length = nums.length;
        if (length <= 1) {
            return length;
        }
        int currIdx = 0;
        int compareIdx = 1;
        int lastInt = nums[currIdx];
         while (compareIdx < length) {
             if (lastInt == nums[compareIdx]) {
                compareIdx++;
            } else {
                lastInt = nums[compareIdx];
                nums[++currIdx] = nums[compareIdx++];
            }
        }
        return currIdx + 1;
    }

    public static void main(String[] args) {
        int[] nums = {0,0,1,1,1,2,2,3,3,4};
        RemoveDuplicates r = new RemoveDuplicates();
        int ret = r.removeDuplicates(nums);
        System.out.println(ret);
    }
}
