package com.butent.bee.egg.client.pst;

import com.google.gwt.event.logical.shared.HighlightEvent;

import com.butent.bee.egg.client.pst.TableEvent.Cell;

public class CellHighlightEvent extends HighlightEvent<Cell> {
  public CellHighlightEvent(int rowIndex, int cellIndex) {
    this(new Cell(rowIndex, cellIndex));
  }

  public CellHighlightEvent(Cell cell) {
    super(cell);
  }
}
