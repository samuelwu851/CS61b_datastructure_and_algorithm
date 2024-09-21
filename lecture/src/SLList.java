/**
 * An SLList is a list of integers, which hides the terrible truth of the
 * nakedness within
 */
public class SLList<T> {

//    private StuffNode first;
    // The first item (if it exists) is at sentinel.next
    private StuffNode sentinel;
    private int size;


    public class StuffNode {
        public T item;
        public StuffNode next;
        public StuffNode(T i,  StuffNode n){
            item = i;
            next = n;
        }

    }

//    public SLList() {
//        sentinel = new StuffNode(71, null);
//        size = 0;
//    }

    public SLList(T x) {
        sentinel = new StuffNode(x, null);
        size = 1;
    }

    public void addFirst(T x){
        sentinel.next = new StuffNode(x, sentinel.next);
        size += 1;
    }

    public T getFirst(){
        return sentinel.next.item;
    }

    public void addLast(T x){
        size += 1;
        StuffNode p = sentinel;
        while(p.next != null){
            p = p.next;
        }
        p.next = new StuffNode(x, null);
    }

//    private static int size(StuffNode p){
//        if(p.next == null){
//            return 1;
//        }
//        return 1 + size(p.next);
//    }

//    public int size(){
//        return size(first);
//    }
    public int size(){
        return size;
    }

    public static void main(String[] args) {
        SLList L = new SLList(10);
        L.addFirst(5);
        L.addFirst(1);
        L.addLast(20);
        System.out.println(L.getFirst());
        System.out.println(L.size());
    }


}
