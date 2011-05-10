package com.butent.bee.shared.data.cache;

import com.google.common.collect.Multimap;

import com.butent.bee.shared.Assert;

import java.util.Collection;
import java.util.List;

/**
 * Contains a list of possible cache objects replacement management types, for example first in
 * first out or least recently used.
 */

public enum ReplacementPolicy {
  LEAST_FREQUENTLY_USED(true, false),
  LEAST_RECENTLY_USED(true, false),
  LEAST_RECENTLY_USED_2(true, false),
  TWO_QUEUES(true, false),
  ADAPTIVE_REPLACEMENT_CACHE(true, false),
  MOST_RECENTLY_USED(false, true),
  FIRST_IN_FIRST_OUT(false, false),
  SECOND_CHANCE(true, false),
  CLOCK(true, false),
  SIMPLE_TIME_BASED(true, false),
  EXTENDED_TIME_BASED(true, false),
  SLIDING_TIME_BASED(true, false),
  RANDOM(false, false);

  private final boolean requiresHistory;
  private final boolean addFirst;

  private ReplacementPolicy(boolean requiresHistory, boolean addFirst) {
    this.requiresHistory = requiresHistory;
    this.addFirst = addFirst;
  }

  public boolean addFirst() {
    return addFirst;
  }

  public boolean addLast() {
    return !addFirst;
  }

  public <K> int getEvictionIndex(final List<K> keys, final Multimap<K, Long> history) {
    Assert.notEmpty(keys);
    int size = keys.size();
    if (size <= 1 || history == null || history.isEmpty()) {
      return 0;
    }

    int idx;
    switch (this) {
      case LEAST_FREQUENTLY_USED:
        int minIdx = 0;
        Collection<Long> hits = history.get(keys.get(minIdx));
        if (hits == null) {
          return minIdx;
        }
        int minCnt = hits.size();
        if (minCnt <= 0) {
          return minIdx;
        }

        int cnt;
        for (int i = minIdx + 1; i < size; i++) {
          hits = history.get(keys.get(i));
          if (hits == null) {
            minIdx = i;
            break;
          }

          cnt = hits.size();
          if (cnt <= 0) {
            minIdx = i;
            break;
          }
          if (cnt < minCnt) {
            minIdx = i;
            minCnt = cnt;
          }
        }

        idx = minIdx;
        break;

      default:
        idx = 0;
    }
    return idx;
  }

  public boolean isHistoryRequired() {
    return requiresHistory;
  }
}
