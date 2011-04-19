package com.butent.bee.shared;

import com.google.common.collect.Lists;

import java.util.List;

public class ArraySequence<T> extends AbstractSequence<T> {
  private T[] values;
  private int length; 

  public ArraySequence(T[] values) {
    Assert.notNull(values);
    this.values = values;
    this.length = values.length;
  }

  public void clear() {
    this.length = 0;
  }

  public T get(int index) {
    assertIndex(index);
    return values[index];
  }

  public Pair<T[], Integer> getArray(T[] a) {
    return new Pair<T[], Integer>(values, getLength());
  }

  public int getLength() {
    return length;
  }

  public List<T> getList() {
    List<T> list = Lists.newArrayListWithCapacity(getLength());
    if (getLength() > 0) {
      for (int i = 0; i < getLength(); i++) {
        list.add(get(i));
      }
    }
    return list;
  }

  public void insert(int index, T value) {
    assertInsert(index);
    List<T> list = getList();
    list.add(index, value);
    setValues(list);
  }

  public void remove(int index) {
    assertIndex(index);
    List<T> list = getList();
    list.remove(index);
    setValues(list);
  }

  public void set(int index, T value) {
    assertIndex(index);
    values[index] = value;
  }

  public void setValues(List<T> lst) {
    Assert.notNull(lst);
    values = lst.toArray(values);
    length = lst.size();
  }

  public void setValues(T[] arr) {
    Assert.notNull(arr);
    values = arr;
    length = values.length;
  }
}
