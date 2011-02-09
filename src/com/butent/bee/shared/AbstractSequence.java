package com.butent.bee.shared;

import java.util.Iterator;

public abstract class AbstractSequence<T> implements Sequence<T> {

  private class SequenceIterator implements Iterator<T> {
    private int index = -1;

    public boolean hasNext() {
      return index < (length() - 1);
    }

    public T next() {
      if (index >= length()) {
        Assert.untouchable();
      }
      return get(++index);
    }

    public void remove() {
      Assert.state(index >= 0 && index < length());
      AbstractSequence.this.remove(index--);
    }
  }
  
  public void add(T value) {
    insert(length(), value);
  }
  
  public Iterator<T> iterator() {
    return new SequenceIterator();
  }
}
