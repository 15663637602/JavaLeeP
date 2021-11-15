package ltcode.linkedLists.reverse.reverseBetween92;

import ds.ListNode;

public class ReverseBetween {
    public ListNode reverseBetween2(ListNode head, int left, int right) {
        if (left == 1) {
            return reverseFirstN(head, right);
        }
        head.next = reverseBetween2(head.next, left - 1, right - 1);
        return head;
    }

    ListNode processor;
    private ListNode reverseFirstN(ListNode head, int n) {
        if (n == 1) {
            processor = head.next;
            return head;
        }
        ListNode newHead = reverseFirstN(head.next, n - 1);
        head.next.next = head;
        head.next = processor;
        return newHead;
    }

}
