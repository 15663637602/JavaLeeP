package ltcode.bfs.medium.openLock752;

import java.util.*;

public class OpenLock {
    public int openLock(String[] deadends, String target) {
        int res = 0;
        Queue<String> q = new LinkedList<>();
        q.offer("0000");
        Set<String> deads = new HashSet<>(Arrays.asList(deadends));
        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                String str = q.poll();
                if (deads.contains(str)) {
                    continue;
                }
                if (target.equals(str)) {
                    return res;
                }
                deads.add(str);
                for (int j = 0; j < 4; j++) {
                    q.offer(getStrUp(str, j));
                    q.offer(getStrDown(str, j));
                }
            }
            res++;
        }
        return -1;
    }

    private String getStrDown(String str, int j) {
        char[] chars = str.toCharArray();
        if (chars[j] == '0') {
            chars[j] = '9';
        } else {
            chars[j]--;
        }
        return String.valueOf(chars);
    }

    private String getStrUp(String str, int j) {
        char[] chars = str.toCharArray();
        if (chars[j] == '9') {
            chars[j] = '0';
        } else {
            chars[j]++;
        }
        return String.valueOf(chars);
    }

    public static void main(String[] args) {
        OpenLock m = new OpenLock();
        int res = m.openLock(new String[]{"8887","8889","8878","8898","8788","8988","7888","9888"}, "8888");
        System.out.println(res);
    }
}
