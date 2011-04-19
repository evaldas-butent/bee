package com.butent.bee.shared;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class ListSequence<T> extends AbstractSequence<T> {
  private List<T> values;
    
  public ListSequence(int size) {
    Assert.nonNegative(size);
    this.values = Lists.newArrayListWithCapacity(size);
    for (int i = 0; i < size; i++) {
      this.values.add(null);
    }
  }

  public ListSequence(List<T> values) {
    Assert.notNull(values);
    this.values = values;
  }

  public void clear() {
    values.clear();
  }

  public T get(int index) {
    assertIndex(index);
    return values.get(index);
  }

  public Pair<T[], Integer> getArray(T[] a) {
    Assert.notNull(a);
    return new Pair<T[], Integer>(values.toArray(a), getLength());
  }

  public int getLength() {
    return values.size();
  }

  public List<T> getList() {
    return values;
  }

  public void insert(int index, T value) {
    assertInsert(index);
    values.add(index, value);
  }

  public void remove(int index) {
    assertIndex(index);
    values.remove(index);
  }

  public void set(int index, T value) {
    assertIndex(index);
    values.set(index, value);
  }

  public void setValues(List<T> lst) {
    Assert.notNull(lst);
    values = lst;
  }

  public void setValues(T[] arr) {
    Assert.notNull(arr);
    setValues(Arrays.asList(arr));
  }
}
