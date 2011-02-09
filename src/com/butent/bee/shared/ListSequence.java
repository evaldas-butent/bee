package com.butent.bee.shared;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class ListSequence<T> extends AbstractSequence<T> {
  private List<T> values;
    
  public ListSequence(int size) {
    super();
    this.values = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      this.values.add(null);
    }
  }

  public ListSequence(List<T> values) {
    super();
    this.values = values;
  }

  @SuppressWarnings("unused")
  private ListSequence() {
  }

  public void clear() {
    values.clear();
  }

  public T get(int index) {
    return values.get(index);
  }

  @SuppressWarnings("unchecked")
  public T[] getArray() {
    return (T[]) values.toArray();
  }

  public List<T> getList() {
    return values;
  }

  public void insert(int index, T value) {
    values.add(index, value);
  }

  public int length() {
    return values.size();
  }

  public void remove(int index) {
    values.remove(index);
  }

  public void set(int index, T value) {
    values.set(index, value);
  }

  public void setValues(List<T> lst) {
    values = lst;
  }

  @Override
  public void setValues(T[] arr) {
    setValues(Arrays.asList(arr));
  }
}
