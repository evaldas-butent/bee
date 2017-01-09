package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.time.YearQuarter;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Objects;

public final class AnalysisSplitValue implements BeeSerializable {

  public static AnalysisSplitValue absent() {
    return new AnalysisSplitValue();
  }

  public static AnalysisSplitValue of(Integer v) {
    if (v == null) {
      return new AnalysisSplitValue();
    } else {
      return new AnalysisSplitValue(BeeUtils.toString(v));
    }
  }

  public static AnalysisSplitValue of(YearMonth ym) {
    if (ym == null) {
      return new AnalysisSplitValue();
    } else {
      return new AnalysisSplitValue(ym.serialize());
    }
  }

  public static AnalysisSplitValue of(YearQuarter yq) {
    if (yq == null) {
      return new AnalysisSplitValue();
    } else {
      return new AnalysisSplitValue(yq.serialize());
    }
  }

  public static AnalysisSplitValue of(String value, Long id) {
    return new AnalysisSplitValue(value, id);
  }

  public static AnalysisSplitValue restore(String s) {
    AnalysisSplitValue asv = new AnalysisSplitValue();
    asv.deserialize(s);
    return asv;
  }

  private enum Serial {
    VALUE, ID, BACKGROUND, FOREGROUND
  }

  private String value;
  private Long id;

  private String background;
  private String foreground;

  private AnalysisSplitValue() {
    this(null);
  }

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

  private void setValue(String value) {
    this.value = value;
  }

  private void setId(Long id) {
    this.id = id;
  }

  public Integer getYear() {
    return BeeUtils.toIntOrNull(value);
  }

  public YearMonth getYearMonth() {
    return YearMonth.parse(value);
  }

  public YearQuarter getYearQuarter() {
    return YearQuarter.parse(value);
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getValue());
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, Serial.values().length);

    for (int i = 0; i < arr.length; i++) {
      String v = arr[i];

      if (!BeeUtils.isEmpty(v)) {
        switch (Serial.values()[i]) {
          case VALUE:
            setValue(v);
            break;
          case ID:
            setId(BeeUtils.toLongOrNull(v));
            break;

          case BACKGROUND:
            setBackground(v);
            break;
          case FOREGROUND:
            setForeground(v);
            break;
        }
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AnalysisSplitValue)) {
      return false;
    }

    AnalysisSplitValue that = (AnalysisSplitValue) o;
    return Objects.equals(value, that.value) && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (id != null ? id.hashCode() : 0);
    return result;
  }

  @Override
  public String serialize() {
    Object[] arr = new Object[Serial.values().length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case VALUE:
          arr[i++] = getValue();
          break;
        case ID:
          arr[i++] = getId();
          break;

        case BACKGROUND:
          arr[i++] = getBackground();
          break;
        case FOREGROUND:
          arr[i++] = getForeground();
          break;
      }
    }

    return Codec.beeSerialize(arr);
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return BeeConst.EMPTY;
    } else {
      return BeeUtils.joinWords(getValue(), getId(), getBackground(), getForeground());
    }
  }
}
