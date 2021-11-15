package ltcode.linkedLists.reverse.reverseKGroup25;

import ds.ListNode;

public class ReverseKGroup {

    public ListNode reverse(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode tmp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = tmp;
        }
        return prev;
    }

    // 反转链表的 [a, b) 区间
    public ListNode reverse(ListNode a, ListNode b) {
        ListNode prev = null;
        ListNode curr = a;
        while (curr != b) {
            ListNode tmp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = tmp;
        }
        return prev;
    }

    public ListNode reverseKGroup(ListNode head, int k) {
        if (head == null || k <= 1) {
            return head;
        }
        // 区间 [a, b) 包含 k 个待反转元素
        ListNode a = head, b = head;
        for (int i = 0; i < k; i++) {
            // 不足 k 个，不需要反转，base case
            if (b == null) {
                return head;
            }
            b = b.next;
        }
        // 反转前 k 个元素
        ListNode newHead = reverse(a, b);
        a.next = reverseKGroup(b, k);
        return newHead;
    }
}
