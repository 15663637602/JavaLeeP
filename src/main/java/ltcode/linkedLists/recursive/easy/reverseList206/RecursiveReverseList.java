package ltcode.linkedLists.recursive.easy.reverseList206;

import ds.ListNode;

public class RecursiveReverseList {
    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null) {
            return head; // 其实就是newHead
        }
        ListNode newHead = reverseList(head.next);
        head.next.next = head;
        head.next = null; // 这里相当于每次递归执行时，都会对当前栈中的head节点的next置空，但是没关系，当该递归栈出栈时，上一层递归中的head.next.next = head; head.next就是被置next节点为空的节点
        return newHead;
    }
}
