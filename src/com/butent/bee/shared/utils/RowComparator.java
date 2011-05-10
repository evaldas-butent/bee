package com.butent.bee.shared.utils;

import com.butent.bee.shared.BeeType;

import java.util.Comparator;

/**
 * Enables row comparison for various data types.
 */

public class RowComparator implements Comparator<Object[]> {
  private int cnt = 0;

  private int[] cols;
  private boolean[] asc;
  private BeeType[] types;

  public RowComparator() {
    this(0, true, BeeType.UNKNOWN);
  }

  public RowComparator(boolean up) {
    this(0, up, BeeType.UNKNOWN);
  }

  public RowComparator(int col) {
    this(col, true, BeeType.UNKNOWN);
  }

  public RowComparator(int col, boolean up) {
    this(col, up, BeeType.UNKNOWN);
  }

  public RowComparator(int col, boolean up, BeeType tp) {
    this.cnt = 1;

    this.cols = new int[1];
    this.asc = new boolean[1];

    if (col >= 0) {
      this.cols[0] = col;
      this.asc[0] = up;
    } else {
      this.cols[0] = -col;
      this.asc[0] = !up;
    }

    this.types = new BeeType[] {tp};
  }

  public RowComparator(int col, BeeType tp) {
    this(col, true, tp);
  }

  public RowComparator(int[] col) {
    this(col, new boolean[] {true}, new BeeType[] {BeeType.UNKNOWN});
  }

  public RowComparator(int[] col, boolean up) {
    this(col, new boolean[] {up}, new BeeType[] {BeeType.UNKNOWN});
  }

  public RowComparator(int[] col, boolean up, BeeType[] tp) {
    this(col, new boolean[] {up}, tp);
  }

  public RowComparator(int[] col, boolean[] up, BeeType tp) {
    this(col, up, new BeeType[] {tp});
  }

  public RowComparator(int[] col, boolean[] up, BeeType[] tp) {
    int c = col.length;

    if (c > 0) {
      this.cnt = c;

      this.cols = new int[c];
      this.asc = new boolean[c];
      this.types = new BeeType[c];

      boolean y = (up != null && up.length > 0) ? up[0] : true;
      BeeType z = (tp != null && tp.length > 0) ? tp[0] : BeeType.UNKNOWN;

      for (int i = 0; i < c; i++) {
        if (i > 0) {
          if (up != null && up.length > i) {
            y = up[i];
          }
          if (tp != null && tp.length > i) {
            z = tp[i];
          }
        }

        if (col[i] >= 0) {
          this.cols[i] = col[i];
          this.asc[i] = y;
        } else {
          this.cols[i] = -col[i];
          this.asc[i] = !y;
        }

        this.types[i] = z;
      }
    }
  }

  public RowComparator(int[] col, BeeType tp) {
    this(col, new boolean[] {true}, new BeeType[] {tp});
  }

  public RowComparator(int[] col, BeeType tp[]) {
    this(col, new boolean[] {true}, tp);
  }

  public int compare(Object[] o1, Object[] o2) {
    if (cnt <= 0) {
      return 0;
    }

    int j;
    BeeType tp;
    int v = 0;

    for (int i = 0; i < cnt; i++) {
      j = cols[i];
      if (j < 0 || j > o1.length || j > o2.length) {
        continue;
      }

      tp = types[i];

      switch (tp) {
        case STRING: {
          v = BeeUtils.compare((String) o1[j], (String) o2[j]);
          break;
        }
        case INT: {
          v = BeeUtils.compare((Integer) o1[j], (Integer) o2[j]);
          break;
        }
        default:
          v = BeeUtils.compare(o1[j], o2[j]);
      }

      if (v != 0) {
        return asc[i] ? v : -v;
      }
    }
    return v;
  }
}
