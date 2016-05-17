package com.butent.bee.shared.data;

import com.butent.bee.shared.Pair;
import com.butent.bee.shared.utils.BeeUtils;

public final class IdPair extends Pair<Long, Long> {

  public static IdPair get(IsRow row, int aIndex, int bIndex) {
    if (row == null) {
      return null;
    } else {
      return new IdPair(row.getLong(aIndex), row.getLong(bIndex));
    }
  }

  public static IdPair of(Long a, Long b) {
    return new IdPair(a, b);
  }

  public static IdPair restoreSerialized(String s) {
    Pair<String, String> pair = Pair.restore(s);

    if (pair == null) {
      return null;
    } else {
      return new IdPair(BeeUtils.toLongOrNull(pair.getA()), BeeUtils.toLongOrNull(pair.getB()));
    }
  }

  private IdPair(Long a, Long b) {
    super(a, b);
  }

  public boolean hasA() {
    return DataUtils.isId(getA());
  }

  public boolean hasB() {
    return DataUtils.isId(getB());
  }
}
