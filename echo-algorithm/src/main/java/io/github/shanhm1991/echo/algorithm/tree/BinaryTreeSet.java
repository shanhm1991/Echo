package io.github.shanhm1991.echo.algorithm.tree;

import java.util.*;

/**
 * 二叉树
 *
 * @author shanhm1991@163.com
 * @param <E>
 */
public class BinaryTreeSet<E extends Comparable<? super E>> implements Set<E> {

  private Node<E> root;

  protected int size;

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public void clear() {
    root = null;
    size = 0;
  }

  protected Node<E> getRoot() {
    return root;
  }

  protected boolean isEmptyNode(Node<E> node) {
    return node == null;
  }

  public E getMin() {
    Node<E> node = getMin(getRoot());
    if (!isEmptyNode(node)) {
      return node.element;
    }
    return null;
  }

  private Node<E> getMin(Node<E> node) {
    if (isEmptyNode(node)) {
      return node;
    }
    while (!isEmptyNode(node.left)) {
      node = node.left;
    }
    return node;
  }

  public E getMax() {
    Node<E> node = getMax(getRoot());
    if (!isEmptyNode(node)) {
      return node.element;
    }
    return null;
  }

  private Node<E> getMax(Node<E> node) {
    if (isEmptyNode(node)) {
      return node;
    }
    while (!isEmptyNode(node.right)) {
      node = node.right;
    }
    return node;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean contains(Object o) {
    return contains((E) o, getRoot());
  }

  private boolean contains(E e, Node<E> node) {
    if (isEmptyNode(node)) {
      return false;
    }

    int compare = e.compareTo(node.element);
    if (compare < 0) {
      return contains(e, node.left);
    } else if (compare > 0) {
      return contains(e, node.right);
    } else {
      return true;
    }
  }

  @Override
  public boolean add(E e) {
    root = add(e, getRoot());
    return true;
  }

  private Node<E> add(E e, Node<E> node) {
    if (isEmptyNode(node)) {
      size++;
      return new Node<>(e);
    }

    int compare = e.compareTo(node.element);
    if (compare < 0) {
      node.left = add(e, node.left);
    } else if (compare > 0) {
      node.right = add(e, node.right);
    } else {
      node.element = e;
    }
    return node;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean remove(Object o) {
    root = remove((E) o, root);
    return true;
  }

  private Node<E> remove(E e, Node<E> node) {
    if (isEmptyNode(node)) {
      return node;
    }

    int compare = e.compareTo(node.element);
    if (compare < 0) {
      node.left = remove(e, node.left);
    } else if (compare > 0) {
      node.right = remove(e, node.right);
    } else if (node.left != null && node.right != null) { // 情形3
      node = removeLeftMax(node); // or node = removeRigthMin(node);
      size--;
    } else { // 情形1或2
      node = (node.left != null) ? node.left : node.right;
      size--;
    }
    return node;
  }

  protected Node<E> removeLeftMax(Node<E> node) {
    Node<E> right = node.right;
    if (node.left.right == null) { // leftMax = node.left
      node = node.left;
      node.right = right;
    } else {
      Node<E> left = node.left;
      node = removeMax(node.left);
      node.right = right;
      node.left = left;
    }
    return node;
  }

  private Node<E> removeMax(Node<E> node) {
    if (node.right != null && node.right.right == null) {
      Node<E> max = node.right;
      node.right = max.left;
      return max;
    }
    assert node.right != null;
    return removeMax(node.right);
  }

  protected Node<E> removeRigthMin(Node<E> node) {
    if (node.right.left == null) { // rightMin = node.right
      Node<E> left = node.left;
      node = node.right;
      node.left = left;
    } else {
      Node<E> right = node.right;
      Node<E> left = node.left;
      node = removeMin(node.right);
      node.right = right;
      node.left = left;
    }
    return node;
  }

  private Node<E> removeMin(Node<E> node) {
    if (node.left != null && node.left.left == null) {
      Node<E> min = node.left;
      node.left = min.right;
      return min;
    }
    assert node.left != null;
    return removeMin(node.left);
  }

  @Override
  public boolean addAll(Collection<? extends E> collection) {
    for (E e : collection) {
      add(e);
    }
    return true;
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    for (Object o : collection) {
      remove(o);
    }
    return true;
  }

  @Override
  public Iterator<E> iterator() {
    return toList().iterator();
  }

  public List<E> toList() {
    List<E> list = new ArrayList<>(size);
    link(getRoot(), list);
    return list;
  }

  private void link(Node<E> node, List<E> list) {
    if (isEmptyNode(node)) {
      return;
    }
    link(node.left, list);
    list.add(node.element);
    link(node.right, list);
  }

  @Override
  public String toString() {
    return toList().toString();
  }

  public void print() {
    Map<Integer, List<Node<E>>> map = new LinkedHashMap<>();
    build(getRoot(), map, 0);
    for (List<Node<E>> list : map.values()) {
      System.out.println(list);
    }
  }

  private void build(Node<E> node, Map<Integer, List<Node<E>>> map, int ht) {
    if (isEmptyNode(node)) {
      return;
    }

    List<Node<E>> list = map.computeIfAbsent(ht, k -> new ArrayList<>());
    list.add(node);
    build(node.left, map, ht + 1);
    build(node.right, map, ht + 1);
  }

  static class Node<E> {

    protected E element;

    protected Node<E> left;

    protected Node<E> right;

    public Node(E element) {
      this.element = element;
    }

    @Override
    public String toString() {
      return element.toString();
    }
  }

  @Override
  public Object[] toArray() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
