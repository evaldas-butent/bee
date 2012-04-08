package com.butent.bee.shared.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class StringList implements List<String> {

  public static final Predicate<String> DEFAULT_PREDICATE = StringPredicate.NOT_EMPTY;

  public static final boolean DEFAULT_UNIQUE = true;
  public static final boolean DEFAULT_CASE_SENSITIVE = false;

  public static final boolean DEFAULT_TRIM_LEFT = true;
  public static final boolean DEFAULT_TRIM_RIGHT = true;

  public static StringList notUniqueCaseInsensitive() {
    return notUniqueCaseInsensitive(DEFAULT_PREDICATE);
  }

  public static StringList notUniqueCaseInsensitive(Predicate<String> predicate) {
    return new StringList(predicate, false, false);
  }

  public static StringList notUniqueCaseSensitive() {
    return notUniqueCaseSensitive(DEFAULT_PREDICATE);
  }

  public static StringList notUniqueCaseSensitive(Predicate<String> predicate) {
    return new StringList(predicate, false, true);
  }

  public static StringList uniqueCaseInsensitive() {
    return uniqueCaseInsensitive(DEFAULT_PREDICATE);
  }

  public static StringList uniqueCaseInsensitive(Predicate<String> predicate) {
    return new StringList(predicate, true, false);
  }

  public static StringList uniqueCaseSensitive() {
    return uniqueCaseSensitive(DEFAULT_PREDICATE);
  }

  public static StringList uniqueCaseSensitive(Predicate<String> predicate) {
    return new StringList(predicate, true, true);
  }

  private final List<String> list = Lists.newArrayList();

  private final Predicate<String> predicate;

  private final boolean unique;
  private final boolean caseSensitive;

  private boolean trimLeft = DEFAULT_TRIM_LEFT;
  private boolean trimRight = DEFAULT_TRIM_RIGHT;

  public StringList() {
    this(DEFAULT_PREDICATE, DEFAULT_UNIQUE, DEFAULT_CASE_SENSITIVE);
  }

  private StringList(Predicate<String> predicate, boolean unique, boolean caseSensitive) {
    super();
    this.predicate = predicate;
    this.unique = unique;
    this.caseSensitive = caseSensitive;
  }

  public boolean accepts(String element) {
    return (predicate == null) ? true : predicate.apply(element);
  }

  public void add(int index, String element) {
    if (!accepts(element)) {
      return;
    }
    if (unique && contains(element)) {
      return;
    }
    
    list.add(index, trim(element));
  }

  public boolean add(String e) {
    if (!accepts(e)) {
      return false;
    }
    if (unique && contains(e)) {
      return false;
    }
    
    return list.add(trim(e));
  }

  public boolean addAll(Collection<? extends String> c) {
    boolean changed = false;
    for (String s : c) {
      changed |= add(s);
    }
    return changed;
  }

  public boolean addAll(int index, Collection<? extends String> c) {
    int z = size();
    for (String s : c) {
      add(index, s);
    }
    return size() > z;
  }

  public void clear() {
    list.clear();
  }

  public boolean contains(Object o) {
    return indexOf(o) >= 0;
  }

  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }

  public String get(int index) {
    return list.get(index);
  }

  public int indexOf(Object o) {
    int index = list.indexOf(o);
    if (index >= 0 || !(o instanceof String) || isEmpty()) {
      return index;
    }

    String s = trim((String) o);
    if (!s.equals(o)) {
      index = list.indexOf(s);
      if (index >= 0) {
        return index;
      }
    }

    if (!caseSensitive) {
      String normalized = s.toLowerCase();
      for (int i = 0; i < size(); i++) {
        if (normalized.equals(normalize(get(i)))) {
          return i;
        }
      }
    }

    return index;
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public boolean isTrimLeft() {
    return trimLeft;
  }

  public boolean isTrimRight() {
    return trimRight;
  }

  public Iterator<String> iterator() {
    return list.iterator();
  }

  public int lastIndexOf(Object o) {
    int index = list.lastIndexOf(o);
    if (index >= 0 || !(o instanceof String) || isEmpty()) {
      return index;
    }

    String s = trim((String) o);
    if (!s.equals(o)) {
      index = list.lastIndexOf(s);
      if (index >= 0) {
        return index;
      }
    }

    if (!caseSensitive) {
      String normalized = s.toLowerCase();
      for (int i = size() - 1; i >= 0; i--) {
        if (normalized.equals(normalize(get(i)))) {
          return i;
        }
      }
    }

    return index;
  }

  public ListIterator<String> listIterator() {
    return list.listIterator();
  }

  public ListIterator<String> listIterator(int index) {
    return list.listIterator(index);
  }

  public String remove(int index) {
    return list.remove(index);
  }

  public boolean remove(Object o) {
    int index = indexOf(o);
    if (index >= 0) {
      list.remove(index);
      return true;
    } else {
      return false;
    }
  }

  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    for (Object o : c) {
      changed |= remove(o);
    }
    return changed;
  }

  public boolean retainAll(Collection<?> c) {
    boolean modified = false;
    Iterator<String> it = iterator();
    while (it.hasNext()) {
      if (!c.contains(it.next())) {
        it.remove();
        modified = true;
      }
    }
    return modified;
  }

  public String set(int index, String element) {
    if (!accepts(element)) {
      return null;
    }
    if (unique) {
      int x = indexOf(element);
      if (x >= 0 && x != index) {
        String v = list.set(index, trim(element));
        remove(x);
        return v;
      }
    }
    
    return list.set(index, trim(element));
  }

  public StringList setTrimLeft(boolean trimLeft) {
    this.trimLeft = trimLeft;
    return this;
  }

  public StringList setTrimRight(boolean trimRight) {
    this.trimRight = trimRight;
    return this;
  }

  public int size() {
    return list.size();
  }

  public List<String> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

  public Object[] toArray() {
    return list.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  private String normalize(String s) {
    if (s == null) {
      return null;
    } else if (caseSensitive) {
      return trim(s);
    } else {
      return trim(s).toLowerCase();
    }
  }

  private String trim(String s) {
    if (s == null) {
      return null;
    } else if (trimLeft && trimRight) {
      return s.trim();
    } else if (trimLeft) {
      return BeeUtils.trimLeft(s);
    } else if (trimRight) {
      return BeeUtils.trimRight(s);
    } else {
      return s;
    }
  }
}
