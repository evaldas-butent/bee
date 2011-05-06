package com.butent.bee.shared;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * Implements operations of list.
 * 
 * @param <T> type of object
 */
public class ListSequence<T> extends AbstractSequence<T> {
  private List<T> values;

  /**
   * Creates a new list with finite size. All elements of list are setting {@code null}
   * 
   * @param size size of the list;
   */
  public ListSequence(int size) {
    Assert.nonNegative(size);
    this.values = Lists.newArrayListWithCapacity(size);
    for (int i = 0; i < size; i++) {
      this.values.add(null);
    }
  }

  /**
   * Creates a new list with {@code List} type values.
   * 
   * @param values values of the list
   */
  public ListSequence(List<T> values) {
    Assert.notNull(values);
    this.values = values;
  }

  /**
   * Removes all values of the list.
   */
  public void clear() {
    values.clear();
  }

  /**
   * Returns the value of the list by {@code index}. The value {@code index} are between 0 and
   * {@code length() - 1}
   * 
   * @param index index of the list field
   * @return the value of the list by index
   */
  public T get(int index) {
    assertIndex(index);
    return values.get(index);
  }

  /**
   * Converts list to array.
   * 
   * @return converted array of list
   */
  public Pair<T[], Integer> getArray(T[] a) {
    Assert.notNull(a);
    return new Pair<T[], Integer>(values.toArray(a), getLength());
  }

  public int getLength() {
    return values.size();
  }

  /**
   * Returns list of {@code java.util.List} type.
   * 
   * @return list of {@code java.util.List} type
   */
  public List<T> getList() {
    return values;
  }

  /**
   * Inserts a new {@code value} into list by {@code index}. The value of {@code index} are between
   * 0 and {@code length()}
   * 
   * @param index index of value in the list;
   * @param value value there will be insert
   */
  public void insert(int index, T value) {
    assertInsert(index);
    values.add(index, value);
  }

  /**
   * Removes the value of the list by index.
   * 
   * @param index index of value of the list;
   */
  public void remove(int index) {
    assertIndex(index);
    values.remove(index);
  }

  /**
   * Replaces the list values to other {@code value} by {@code index}.
   * 
   * @param index index of value in the list
   * @param value a new value
   */
  public void set(int index, T value) {
    assertIndex(index);
    values.set(index, value);
  }

  /**
   * Sets a values in the list of other {@code lst}.
   * 
   * @param lst the list of new values
   */
  public void setValues(List<T> lst) {
    Assert.notNull(lst);
    values = lst;
  }

  /**
   * Sets a value in the list of the array.
   * 
   * @param arr the array of values
   */
  public void setValues(T[] arr) {
    Assert.notNull(arr);
    setValues(Arrays.asList(arr));
  }
}
