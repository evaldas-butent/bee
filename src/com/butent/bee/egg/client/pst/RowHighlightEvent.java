package com.butent.bee.egg.client.pst;

import com.google.gwt.event.logical.shared.HighlightEvent;

import com.butent.bee.egg.client.pst.TableEvent.Row;

public class RowHighlightEvent extends HighlightEvent<Row> {
  public RowHighlightEvent(int rowIndex) {
    this(new Row(rowIndex));
  }

  public RowHighlightEvent(Row row) {
    super(row);
  }
}
