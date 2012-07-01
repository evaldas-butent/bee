package com.butent.bee.shared.data.cache;

enum CacheImpl {
  LIST, MAP;

  public <K, V> AbstractCache<K, V> create(int maxSize, ReplacementPolicy replacementPolicy) {
    switch (this) {
      case LIST:
        return new ListImpl<K, V>(maxSize, replacementPolicy);
      case MAP:
        return new MapImpl<K, V>(maxSize, replacementPolicy);
    }
    return null;
  }
}
