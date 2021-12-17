package io.github.echo.issue.issue2;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * @author shanhm1991
 *
 */
public class SortLongLinkedList implements Iterable<Long>{
	 
    private int size = 0;
 
    private Node<Long> first;
 
    private Node<Long> last;
 
    //e < first.item && e > last.item
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void linkLongWithSort(long e){
        Node<Long> nextNode = first.next;
        for(;;){
            if(e < nextNode.item){
                nextNode = nextNode.next;
                continue;
            }
            Node<Long> preNode = nextNode.prev;
            Node<Long> newNode = new Node(preNode,e,nextNode);
            preNode.next = newNode;
            nextNode.prev = newNode;
            size++;
            break;
        }
    }
 
    public Long getFirst() {
        final Node<Long> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }
 
    public Long getLast() {
        final Node<Long> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }
 
    public void linkFirst(Long e) {
        final Node<Long> f = first;
        final Node<Long> newNode = new Node<Long>(null, e, f);
        first = newNode;
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;
        size++;
    }
 
    public void linkLast(Long e) {
        final Node<Long> l = last;
        final Node<Long> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
    }
 
    public Long unlinkLast() {
        final Long element = last.item;
        final Node<Long> prev = last.prev;
        last.item = null;
        last.prev = null; // help GC
        
        last = prev;
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        return element;
    }
 
    public boolean isEmpty() {
        return size == 0;
    }
    
    public int size() {
        return size;
    }
 
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;
 
        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
 
    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>(){
            Node<Long> current = first;
 
            @Override
            public boolean hasNext() {
                return current.next != last;
            }
 
            @Override
            public Long next() {
                long l = current.next.item;
                current = current.next;
                return l;
            }
 
            @Override
            public void remove() {
                // TODO Auto-generated method stub
            }
        };
    }
    
    public String toString(){
        StringBuilder builder = new StringBuilder();
        Node<Long> n = first;
        builder.append(n.item);
        while((n = n.next) != last){
            builder.append(",");
            builder.append(n.item);
        }
        builder.append(",");
        builder.append(last.item);
        return builder.toString();
    }
}
