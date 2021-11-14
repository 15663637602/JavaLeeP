package ltcode.linkedLists.cycleList.detectCycle142;

import ds.ListNode;

import java.util.HashSet;
import java.util.Set;

public class DetectCycle {
    public ListNode detectCycle2(ListNode head) {
        if (head == null) {
            return null;
        }
        Set<ListNode> set = new HashSet<>();
        while (head != null) {
            if (set.contains(head)) {
                return head;
            }
            set.add(head);
            head = head.next;
        }
        return null;
    }

    public ListNode detectCycle(ListNode head) {
        if (head == null) {
            return null;
        }
        ListNode slow = head;
        ListNode fast = head;
        ListNode node = null;

        while (true) {
            if (slow.next == null) {
                break;
            }
            if (fast.next == null || fast.next.next == null) {
                break;
            }
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                node = slow;
                break;
            }
        }
        if (node == null) {
            return null;
        }

        slow = head;
        while (slow != node) {
            slow = slow.next;
            node = node.next;
        }
        return slow;
    }
}
