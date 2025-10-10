/** 
 * Builds a singly linked list of size 5 and prints it to the console.
 * 
 * @author Jochen Lang
 */

class DoubleLinkedList {
    DNode llist;
	DNode tail;

    DoubleLinkedList(int sz ) {
		if ( sz <= 0 ) {
			this.llist = null;
			this.tail = null;
		}
		else {
			// start with list of size 1
			llist = new DNode( "0", null, null);
			DNode current = llist; // temp node for loop
			// add further nodes
			for ( int i=1; i<sz; ++i ) {
			// create node and attach it to the list
			DNode DNode2Add = new DNode( Integer.toString(i), null, current);
			tail = DNode2Add;
			current.setNext(DNode2Add);   // add first node
			current=DNode2Add;
			}
		}
    }
    
    /**
     * Print all the elements of the list assuming that they are Strings
     */
    public void print() {
		/* Print the list */
		DNode current = llist; // point to the first node
		while (current != null) {
			System.out.print((String)current.getElement() + " ");
			current = current.getNext(); // move to the next
		}
		System.out.println();
		}

    public void deleteFirst() {
		if ( llist != null ) {
			llist = llist.getNext();
			llist.setPrevious(null);
			// remove the next's prev pointer and make it the
		}
    }

    public void deleteLast() {
		if ( llist == null ) return; // no node

		if ( llist.getNext() == null ) { // only 1 node
			llist = null;
			tail = null;
			return;
		}

		else {
			DNode lastNode = tail;
			tail = lastNode.getPrevious();
			tail.setNext(null);
			return;
		}
    }

    // create and display a linked list
    public static void main(String [] args){
		/* Create the list */
		DoubleLinkedList llist = new DoubleLinkedList( 5 );
		/* Print the list */
		llist.print();
		/* delete first and print */
		llist.deleteFirst();
		llist.print();
		/* delete last and print 5 times */
		for ( int i=0; i< 5; ++i ) {
			llist.deleteLast();
			llist.print();
		}
    }
}
