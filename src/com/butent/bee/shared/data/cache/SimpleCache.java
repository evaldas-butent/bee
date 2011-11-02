package com.butent.bee.shared.data.cache;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Enables to store frequently used data into memory, contains usage attributes like hit count or
 * last hit.
 */

public class SimpleCache<K, V> implements HasInfo {
  private final LinkedList<K> keys = Lists.newLinkedList();
  private final LinkedList<V> values = Lists.newLinkedList();

  private int hitCount = 0;
  private int missCount = 0;

  private int addCount = 0;
  private int remCount = 0;

  private long lastHit = 0;
  private long lastMiss = 0;

  private long lastAdd = 0;
  private long lastRem = 0;

  private final Multimap<K, Long> history;

  private int maxSize;
  private final ReplacementPolicy replacementPolicy;

  public SimpleCache(int maxSize, ReplacementPolicy replacementPolicy) {
    this.maxSize = maxSize;
    this.replacementPolicy = replacementPolicy;

    if (replacementPolicy == null || !replacementPolicy.isHistoryRequired()) {
      this.history = null;
    } else {
      this.history = ArrayListMultimap.create();
    }
  }

  public int getAddCount() {
    return addCount;
  }

  public double getFillRatio() {
    if (maxSize <= 0) {
      return -1.0;
    }
    int size = getSize();
    if (size > 0) {
      return (double) size / (double) maxSize;
    } else {
      return 0.0;
    }
  }

  public int getHistorySize() {
    if (history == null) {
      return BeeConst.UNDEF;
    } else {
      return history.size();
    }
  }

  public int getHitCount() {
    return hitCount;
  }

  public double getHitRatio() {
    if (hitCount > 0) {
      return (double) hitCount / (double) (hitCount + missCount);
    } else {
      return 0.0;
    }
  }

  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Max Size", getMaxSize(),
        "Keys Size", keys.size(), "Values Size", values.size(),
        "Is Full", isFull(), "Fill Ratio", getFillRatio(),
        "Replacement Policy", getReplacementPolicy(), "History Size", getHistorySize(),
        "Add Count", getAddCount(), "Rem Count", getRemCount(),
        "Last Add", transformTime(getLastAdd()), "Last Rem", transformTime(getLastRem()),
        "Hit Count", getHitCount(), "Miss Count", getMissCount(), "Hit Ratio", getHitRatio(),
        "Last Hit", transformTime(getLastHit()), "Last Miss", transformTime(getLastMiss()));
  }

  public long getLastAdd() {
    return lastAdd;
  }

  public long getLastHit() {
    return lastHit;
  }

  public long getLastMiss() {
    return lastMiss;
  }

  public long getLastRem() {
    return lastRem;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public int getMissCount() {
    return missCount;
  }

  public int getRemCount() {
    return remCount;
  }

  public ReplacementPolicy getReplacementPolicy() {
    return replacementPolicy;
  }

  public int getSize() {
    return keys.size();
  }

  public boolean isEmpty() {
    return getSize() <= 0;
  }

  public boolean isFull() {
    if (maxSize > 0) {
      return getSize() >= maxSize;
    } else {
      return false;
    }
  }

  protected synchronized void add(K key, V value) {
    addCount++;
    lastAdd = currentTime();

    int idx = keys.indexOf(key);
    if (idx >= 0) {
      values.set(idx, value);
      return;
    }

    if (isFull()) {
      remove();
    }

    if (replacementPolicy == null || replacementPolicy.addLast()) {
      keys.addLast(key);
      values.addLast(value);
    } else {
      keys.addFirst(key);
      values.addFirst(value);
    }
  }

  protected synchronized void clearHistory() {
    if (history != null) {
      history.clear();
    }
  }

  protected boolean contains(K key) {
    return keys.contains(key);
  }
  
  protected synchronized boolean deleteKey(K key) {
    int idx = keys.indexOf(key);
    if (idx >= 0) {
      keys.remove(idx);
      values.remove(idx);
      if (history != null) {
        history.removeAll(key);
      }
      return true;
    } else {
      return false;
    }
  }

  protected synchronized int deleteValue(V value) {
    int idx = values.indexOf(value);
    if (idx >= 0) {
      K key = keys.remove(idx);
      values.remove(idx);
      if (history != null) {
        history.removeAll(key);
      }
      return deleteValue(value) + 1;
    } else {
      return 0;
    }
  }
  
  protected synchronized V get(K key) {
    int idx = keys.indexOf(key);
    if (idx >= 0) {
      V value = values.get(idx);
      setHit(key, idx);
      return value;
    } else {
      setMiss();
      return null;
    }
  }

  protected void invalidate() {
    clear();
  }

  protected void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  private void assertIndex(int idx) {
    Assert.betweenExclusive(idx, 0, getSize());
  }

  private synchronized void clear() {
    keys.clear();
    values.clear();
    hitCount = 0;
    missCount = 0;
    clearHistory();
  }

  private long currentTime() {
    return System.currentTimeMillis();
  }

  private void moveToBack(int idx) {
    assertIndex(idx);
    if (idx == getSize()) {
      return;
    }

    K key = keys.get(idx);
    V value = values.get(idx);

    keys.remove(idx);
    values.remove(idx);

    keys.add(key);
    values.add(value);
  }

  private void remove() {
    remCount++;
    lastRem = currentTime();

    int size = getSize();
    if (size <= 1) {
      keys.clear();
      values.clear();
      return;
    }

    int idx = 0;

    if (replacementPolicy != null) {
      switch (replacementPolicy) {
        case RANDOM:
          idx = BeeUtils.randomInt(0, size);
          break;
        case FIRST_IN_FIRST_OUT:
        case LEAST_RECENTLY_USED:
        case LEAST_RECENTLY_USED_2:
          idx = 0;
          break;
        case MOST_RECENTLY_USED:
          idx = size - 1;
          break;
        default:
          idx = replacementPolicy.getEvictionIndex(keys, history);
      }
    }

    assertIndex(idx);
    keys.remove(idx);
    values.remove(idx);
  }

  private void setHit(K key, int idx) {
    hitCount++;
    lastHit = currentTime();

    if (history != null) {
      history.put(key, currentTime());
    }

    if (replacementPolicy == null) {
      return;
    }
    switch (replacementPolicy) {
      case LEAST_RECENTLY_USED:
      case LEAST_RECENTLY_USED_2:
        moveToBack(idx);
        break;
      default:
    }
  }

  private void setMiss() {
    missCount++;
    lastMiss = currentTime();
  }

  private String transformTime(long time) {
    if (time <= 0) {
      return BeeConst.STRING_MINUS;
    } else {
      return new DateTime(time).toString();
    }
  }
}
