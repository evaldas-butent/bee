package com.butent.bee.egg.client.grid.event;

import com.butent.bee.egg.client.grid.event.TableEvent.Cell;

public class CellUnhighlightEvent extends UnhighlightEvent<Cell> {
  public CellUnhighlightEvent(int rowIndex, int cellIndex) {
    this(new Cell(rowIndex, cellIndex));
  }

  public CellUnhighlightEvent(Cell cell) {
    super(cell);
  }

}
