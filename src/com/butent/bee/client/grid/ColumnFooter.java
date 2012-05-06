package com.butent.bee.client.grid;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.client.grid.cell.FooterCell;

import java.util.Collection;
import java.util.List;

public class ColumnFooter extends Header<String> {

  private final List<String> sources;

  public ColumnFooter(List<String> sources, Collection<String> updaterEvents, 
      final ValueUpdater<String> valueUpdater) {
    super(new FooterCell(updaterEvents));
    this.sources = sources;

    setUpdater(valueUpdater);
  }

  public List<String> getSources() {
    return sources;
  }

  @Override
  public String getValue() {
    return ((FooterCell) getCell()).getNewValue();
  }
}
