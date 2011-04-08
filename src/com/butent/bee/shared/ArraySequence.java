package com.butent.bee.shared;

import com.butent.bee.shared.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArraySequence<T> extends AbstractSequence<T> {
  private T[] values;

  public ArraySequence(T[] values) {
    super();
    this.values = values;
  }

  @SuppressWarnings("unused")
  private ArraySequence() {
  }

  public void clear() {
    setValues(new ArrayList<T>());
  }

  public T get(int index) {
    return values[index];
  }

  public T[] getArray() {
    return values;
  }

  public List<T> getList() {
    return Arrays.asList(getArray());
  }

  public void insert(int index, T value) {
    setValues(ArrayUtils.insert(getArray(), index, value));
  }

  public int length() {
    return values.length;
  }

  public void remove(int index) {
    setValues(ArrayUtils.remove(getArray(), index));
  }

  public void set(int index, T value) {
    values[index] = value;
  }

  public void setValues(List<T> lst) {
    setValues(lst.toArray(getArray()));
  }

  public void setValues(T[] arr) {
    values = arr;
  }
}
