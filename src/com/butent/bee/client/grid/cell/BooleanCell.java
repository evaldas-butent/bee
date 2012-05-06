package com.butent.bee.client.grid.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables using a cell for representing boolean data.
 */

public class BooleanCell extends AbstractCell<Boolean> {

  public static Character defaultTrueChar = BeeConst.CHECK_MARK;
  public static Character defaultFalseChar = null;
  public static Character defaultNullChar = null;
  
  public static String format(Boolean value) {
    Character ch = (value == null) ? defaultNullChar : (value ? defaultTrueChar : defaultFalseChar);
    if (ch == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.toString(ch);
    }
  }
  
  private Character trueChar;
  private Character falseChar;
  private Character nullChar;

  public BooleanCell() {
    this(defaultTrueChar, defaultFalseChar, defaultNullChar);
  }
  
  public BooleanCell(Character trueChar, Character falseChar) {
    this(trueChar, falseChar, defaultNullChar);
  }
  
  public BooleanCell(Character trueChar, Character falseChar, Character nullChar) {
    this.trueChar = trueChar;
    this.falseChar = falseChar;
    this.nullChar = nullChar;
  }

  public Character getFalseChar() {
    return falseChar;
  }

  public Character getNullChar() {
    return nullChar;
  }

  public Character getTrueChar() {
    return trueChar;
  }

  @Override
  public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
    Character ch = (value == null) ? nullChar : (value ? trueChar : falseChar);
    if (ch != null) {
      sb.append(ch);
    }
  }

  public void setFalseChar(Character falseChar) {
    this.falseChar = falseChar;
  }

  public void setNullChar(Character nullChar) {
    this.nullChar = nullChar;
  }

  public void setTrueChar(Character trueChar) {
    this.trueChar = trueChar;
  }
}
