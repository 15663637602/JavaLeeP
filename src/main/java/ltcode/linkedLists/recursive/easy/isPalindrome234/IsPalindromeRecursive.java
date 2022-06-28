package ltcode.linkedLists.recursive.easy.isPalindrome234;

import ds.ListNode;

public class IsPalindromeRecursive {

    ListNode left;
    public boolean isPalindrome(ListNode head) {
        left = head;
        return traverse(head);
    }

    private boolean traverse(ListNode right) {
        if (right == null) {
            return true;
        }
        boolean ret = traverse(right.next);
        ret = ret && left.val == right.val;
        left = left.next;
        return ret;
    }

    public boolean isPalindrome2(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
        }
        if (fast != null) {
            slow = slow.next;
        }
        ListNode left = head;
        ListNode right = reverseList(slow);
        while (right != null) {
            if (left.val != right.val) {
                return false;
            }
            left = left.next;
            right = right.next;
        }
        return true;
    }

    private ListNode reverseList(ListNode head) {
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

    public static void main(String[] args) {
        ListNode head = new ListNode(1);
        head.next = new ListNode(2);
        head.next.next = new ListNode(2);
        head.next.next.next = new ListNode(1);
        head.next.next.next.next = new ListNode(33);
        IsPalindromeRecursive isPalindromeRecursive = new IsPalindromeRecursive();
        boolean ret = isPalindromeRecursive.isPalindrome(head);
        System.out.println(ret);
    }
}
