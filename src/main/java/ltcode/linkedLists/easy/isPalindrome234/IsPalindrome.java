package ltcode.linkedLists.easy.isPalindrome234;

import ds.ListNode;

public class IsPalindrome {

    void traverse(ListNode head) {
        // 前序遍历代码
        traverse(head.next);
        // 后序遍历代码
    }

    /* 倒序打印单链表中的元素值 */
    void printByDescOrder(ListNode head) {
        if (head == null) return;
        traverse(head.next);
        // 后序遍历代码
        System.out.println(head.val);
    }


    public boolean isPalindrome(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            fast = fast.next.next;
            slow = slow.next;
        }
        ListNode mid = slow.next;
        ListNode reversedHead = reverse(mid);
        while (reversedHead != null) {
            if (head.val != reversedHead.val) {
                return false;
            }
            reversedHead = reversedHead.next;
            head = head.next;
        }
        return true;
    }

    private ListNode reverse(ListNode head) {
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
        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(1);
        ListNode n3 = new ListNode(2);
        ListNode n4 = new ListNode(1);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        IsPalindrome i = new IsPalindrome();
        boolean res = i.isPalindrome(n1);
        System.out.println(res);
    }
}
