package ltcode.backtrack.medium.permute.permute46;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Permutation {
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        LinkedList<Integer> passed = new LinkedList<>();
        backtrack(nums, passed, res);
        return res;
    }

    private void backtrack(int[] nums, LinkedList<Integer> passed, List<List<Integer>> res) {
        if (nums.length == passed.size()) {
            res.add(passed);
            return;
        }
        for (int num : nums) {
            if (passed.contains(num)) {
                continue;
            }
            LinkedList<Integer> newPassed = new LinkedList<>(passed);
            newPassed.add(num);
            backtrack(nums, newPassed, res);
        }
    }

    private void backtrack2(int[] nums, LinkedList<Integer> passed, List<List<Integer>> res) {
        if (passed.size() == nums.length) {
            res.add(new ArrayList<>(passed));
            return;
        }
        for (int num : nums) {
            if (passed.contains(num)) {
                continue;
            }
            passed.add(num);
            backtrack(nums, passed, res);
            passed.removeLast();
        }
    }
}
