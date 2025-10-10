/**
 * A simple node class for a singly-linked list.  Each node has a
 * reference to a stored element and a next node.
 * This class is based on the <code>DNode</code> class by Roberto Tamassia.
 *
 * @author Jochen Lang
 */

public class DNode {
  private Object element;
  private DNode next;
  private DNode previous;
  DNode() { this(null, null, null); }
    DNode(Object e, DNode n, DNode p) {
    this.element = e;
    this.next = n;
    this.previous = p;
  }
  public void setElement(Object newElem) { element = newElem; }
  public void setNext(DNode newNext) { next = newNext; }
  public void setPrevious(DNode newPrevious) { previous = newPrevious; }
  public Object getElement() { return element; }
  public DNode getNext() { return next; }
  public DNode getPrevious() { return previous; }
}