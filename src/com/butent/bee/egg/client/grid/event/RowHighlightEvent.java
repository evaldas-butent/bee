package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.logical.shared.HighlightEvent;

import com.butent.bee.egg.client.grid.event.TableEvent.Row;

public class RowHighlightEvent extends HighlightEvent<Row> {
  public RowHighlightEvent(int rowIndex) {
    this(new Row(rowIndex));
  }

  public RowHighlightEvent(Row row) {
    super(row);
  }
}
