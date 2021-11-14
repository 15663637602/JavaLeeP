package ltcode.backtrack.medium.permute.lettercasePermute784;

import java.util.ArrayList;
import java.util.List;

public class LetterCasePermute {
    public List<String> letterCasePermutation(String s) {
        int len = s.length();
        List<String> res = new ArrayList<>();
        if (len == 0) {
            return res;
        }
        StringBuilder path = new StringBuilder();
        dfs(s, path, res);
        return res;
    }

    private void dfs(String s, StringBuilder path, List<String> res) {
        if (s.length() == 0) {
            res.add(path.toString());
            return;
        }
        char c = s.charAt(0);
        if (Character.isDigit(c)) {
            StringBuilder newPath = new StringBuilder(path);
            newPath.append(c);
            dfs(s.substring(1), newPath, res);
        } else {
            StringBuilder newPath = new StringBuilder(path);
            newPath.append(c);
            dfs(s.substring(1), newPath, res);
            newPath.deleteCharAt(newPath.length() - 1);
            if (Character.isUpperCase(c)) {
                newPath.append(Character.toLowerCase(c));
            } else {
                newPath.append(Character.toUpperCase(c));
            }
            dfs(s.substring(1), newPath, res);
        }
    }


    public static void main(String[] args) {
        LetterCasePermute l = new LetterCasePermute();
        List<String> ret = l.letterCasePermutation("a1b2");
        System.out.println(ret);
    }
}
