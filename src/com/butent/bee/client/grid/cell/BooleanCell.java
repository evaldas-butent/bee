package com.butent.bee.client.grid.cell;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.HandlesFormat;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables using a cell for representing boolean data.
 */

public class BooleanCell extends AbstractCell<Boolean> implements HandlesFormat {

  private String trueText;
  private String falseText;
  private String nullText;

  public BooleanCell() {
    this(BeeConst.STRING_CHECK_MARK, null, null);
  }

  public BooleanCell(String trueText, String falseText, String nullText) {
    this.trueText = trueText;
    this.falseText = falseText;
    this.nullText = nullText;
  }

  public String getFalseText() {
    return falseText;
  }

  public String getNullText() {
    return nullText;
  }

  public String getTrueText() {
    return trueText;
  }

  @Override
  public String render(CellContext context, Boolean value) {
    return (value == null) ? nullText : (value ? trueText : falseText);
  }

  public void setFalseText(String falseText) {
    this.falseText = falseText;
  }

  @Override
  public void setFormat(String format) {
    if (!BeeUtils.isEmpty(format)) {
      String[] arr = BeeUtils.split(format, BeeConst.CHAR_SPACE);
      int length = ArrayUtils.length(arr);

      if (length > 0) {
        setTrueText(arr[0]);
      }
      if (length > 1) {
        setFalseText(arr[1]);
      }
      if (length > 2) {
        setNullText(arr[2]);
      }
    }
  }

  public void setNullText(String nullText) {
    this.nullText = nullText;
  }

  public void setTrueText(String trueText) {
    this.trueText = trueText;
  }
}
