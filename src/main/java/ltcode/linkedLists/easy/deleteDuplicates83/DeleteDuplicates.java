package ltcode.linkedLists.easy.deleteDuplicates83;

import ds.ListNode;

public class DeleteDuplicates {
    public ListNode deleteDuplicates(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode curr = head;
        ListNode compare = head.next;
        int lastInt = curr.val;
        while (compare != null) {
            if (lastInt != compare.val) {
                lastInt = compare.val;
                curr.next = compare;
                curr = curr.next;
            }
            compare = compare.next;
        }
        curr.next = null;
        return head;
    }
}
