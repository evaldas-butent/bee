package com.butent.bee.egg.client.grid.event;

import com.butent.bee.egg.client.grid.event.TableEvent.Row;

public class RowUnhighlightEvent extends UnhighlightEvent<Row> {
  public RowUnhighlightEvent(int rowIndex) {
    this(new Row(rowIndex));
  }

  public RowUnhighlightEvent(Row row) {
    super(row);
  }
}
