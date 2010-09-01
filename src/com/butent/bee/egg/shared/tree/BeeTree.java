package com.butent.bee.egg.shared.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class BeeTree<T> {

  private T root;

  private ArrayList<BeeTree<T>> leafs = new ArrayList<BeeTree<T>>();

  private BeeTree<T> parent = null;

  private HashMap<T, BeeTree<T>> locate = new HashMap<T, BeeTree<T>>();

  public BeeTree(T root) {
    this.root = root;
    locate.put(root, this);
  }

  public void addLeaf(T parent, T leaf) {
    if (locate.containsKey(parent)) {
      locate.get(parent).addLeaf(leaf);
    } else {
      addLeaf(parent).addLeaf(leaf);
    }
  }

  public BeeTree<T> addLeaf(T leaf) {
    BeeTree<T> t = new BeeTree<T>(leaf);
    leafs.add(t);
    t.parent = this;
    t.locate = this.locate;
    locate.put(leaf, t);
    return t;
  }

  public BeeTree<T> setAsParent(T parentRoot) {
    BeeTree<T> t = new BeeTree<T>(parentRoot);
    t.leafs.add(this);
    this.parent = t;
    t.locate = this.locate;
    t.locate.put(root, this);
    t.locate.put(parentRoot, t);
    return t;
  }

  public T getRoot() {
    return root;
  }

  public BeeTree<T> getTree(T element) {
    return locate.get(element);
  }

  public BeeTree<T> getParent() {
    return parent;
  }

  public Collection<T> getSuccessors(T root) {
    Collection<T> successors = new ArrayList<T>();
    BeeTree<T> tree = getTree(root);
    if (null != tree) {
      for (BeeTree<T> leaf : tree.leafs) {
        successors.add(leaf.root);
      }
    }
    return successors;
  }

  public Collection<BeeTree<T>> getSubTrees() {
    return leafs;
  }

  public static <T> Collection<T> getSuccessors(T of, Collection<BeeTree<T>> in) {
    for (BeeTree<T> tree : in) {
      if (tree.locate.containsKey(of)) {
        return tree.getSuccessors(of);
      }
    }
    return new ArrayList<T>();
  }

  @Override
  public String toString() {
    return printTree(0);
  }

  private static final int indent = 2;

  private String printTree(int increment) {
    String s = "";
    String inc = "";
    for (int i = 0; i < increment; ++i) {
      inc = inc + " ";
    }
    s = inc + root;
    for (BeeTree<T> child : leafs) {
      s += "\n" + child.printTree(increment + indent);
    }
    return s;
  }
}
