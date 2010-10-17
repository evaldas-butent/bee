package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.TableEvent.Row;

public class RowUnhighlightEvent extends UnhighlightEvent<Row> {
  public RowUnhighlightEvent(int rowIndex) {
    this(new Row(rowIndex));
  }

  public RowUnhighlightEvent(Row row) {
    super(row);
  }
}
