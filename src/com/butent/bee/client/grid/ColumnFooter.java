package com.butent.bee.client.grid;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.user.cellview.client.Header;

/**
 * Creates new footer cell.
 */

public class ColumnFooter extends Header<String> {

  private final String source;
  private String value = null;

  public ColumnFooter(String source, final ValueUpdater<String> valueUpdater) {
    super(new FooterCell());
    this.source = source;

    setUpdater(new ValueUpdater<String>() {
      public void update(String newValue) {
        setValue(newValue);
        if (valueUpdater != null) {
          valueUpdater.update(newValue);
        }
      }
    });
  }

  public String getSource() {
    return source;
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
