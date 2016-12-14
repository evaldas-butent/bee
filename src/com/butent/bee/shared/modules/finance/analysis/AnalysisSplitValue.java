package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.time.YearQuarter;
import com.butent.bee.shared.utils.BeeUtils;

public final class AnalysisSplitValue {

  public static AnalysisSplitValue absent() {
    return new AnalysisSplitValue(null);
  }

  public static AnalysisSplitValue of(Integer v) {
    if (v == null) {
      return new AnalysisSplitValue(null);
    } else {
      return new AnalysisSplitValue(BeeUtils.toString(v));
    }
  }

  public static AnalysisSplitValue of(YearMonth ym) {
    if (ym == null) {
      return new AnalysisSplitValue(null);
    } else {
      return new AnalysisSplitValue(ym.serialize());
    }
  }

  public static AnalysisSplitValue of(YearQuarter yq) {
    if (yq == null) {
      return new AnalysisSplitValue(null);
    } else {
      return new AnalysisSplitValue(yq.serialize());
    }
  }

  public static AnalysisSplitValue of(String value, Long id) {
    return new AnalysisSplitValue(value, id);
  }

  private final String value;
  private final Long id;

  private String background;
  private String foreground;

  private AnalysisSplitValue(String value) {
    this(value, null);
  }

  private AnalysisSplitValue(String value, Long id) {
    this.value = value;
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  public Long getId() {
    return id;
  }

  public String getBackground() {
    return background;
  }

  public void setBackground(String background) {
    this.background = background;
  }

  public String getForeground() {
    return foreground;
  }

  public void setForeground(String foreground) {
    this.foreground = foreground;
  }
}
