package com.butent.bee.client.grid;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.user.cellview.client.Header;

import java.util.List;

/**
 * Creates new footer cell.
 */

public class ColumnFooter extends Header<String> {

  private final List<String> sources;
  private String value = null;

  public ColumnFooter(List<String> sources, final ValueUpdater<String> valueUpdater) {
    super(new FooterCell());
    this.sources = sources;

    setUpdater(new ValueUpdater<String>() {
      public void update(String newValue) {
        setValue(newValue);
        if (valueUpdater != null) {
          valueUpdater.update(newValue);
        }
      }
    });
  }

  public List<String> getSources() {
    return sources;
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
