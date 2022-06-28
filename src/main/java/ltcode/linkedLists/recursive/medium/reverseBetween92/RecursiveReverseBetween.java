package ltcode.linkedLists.recursive.medium.reverseBetween92;

import ds.ListNode;

public class RecursiveReverseBetween {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        if (left == 1) {
            return reverseFirstN(head, right);
        }
        head.next = reverseBetween(head.next, left - 1, right - 1);
        return head;
    }

    ListNode successor;
    private ListNode reverseFirstN(ListNode head, int n) {
        if (n == 1) {
            successor = head.next; // 最后要被反转过来的链表尾部 用 .next 接上的那个节点
            return head;
        }
        ListNode newHead = reverseFirstN(head.next, n - 1);
        head.next.next = head;
        head.next = successor; // 平时都是 null, 只有当n = 1时，才被记录为 反转过来的链表尾部 应该拼接的next节点
        return newHead;
    }

}
