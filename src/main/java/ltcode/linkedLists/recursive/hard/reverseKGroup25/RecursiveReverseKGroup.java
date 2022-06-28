package ltcode.linkedLists.recursive.hard.reverseKGroup25;

import ds.ListNode;

public class RecursiveReverseKGroup {
    public ListNode reverseKGroup(ListNode head, int k) {
        if (head == null) {
            return null;
        }
        ListNode start = head, end = head;
        for (int i = 0; i < k; i++) {
            if (end == null) {
                return head;
            }
            end = end.next;
        }
        ListNode newHead = reverseBetween(start, end); // 翻转 [start, end)
        start.next = reverseKGroup(end, k);
        return newHead;
    }

    private ListNode reverseBetween(ListNode start, ListNode end) {
        ListNode prev = null;
        ListNode curr = start;
        while (curr != end) {
            ListNode tmp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = tmp;
        }
        return prev;
    }
}
