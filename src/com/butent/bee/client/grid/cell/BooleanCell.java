package com.butent.bee.client.grid.cell;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.i18n.Format;

/**
 * Enables using a cell for representing boolean data.
 */

public class BooleanCell extends AbstractCell<Boolean> {

  private Character trueChar;
  private Character falseChar;
  private Character nullChar;

  public BooleanCell() {
    this(Format.getDefaultTrueChar(), Format.getDefaultFalseChar(), Format.getDefaultNullChar());
  }

  public BooleanCell(Character trueChar, Character falseChar) {
    this(trueChar, falseChar, Format.getDefaultNullChar());
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
  public void render(CellContext context, Boolean value, SafeHtmlBuilder sb) {
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
