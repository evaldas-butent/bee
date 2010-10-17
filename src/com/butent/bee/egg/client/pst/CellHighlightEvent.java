package com.butent.bee.egg.client.pst;

import com.google.gwt.event.logical.shared.HighlightEvent;

import com.butent.bee.egg.client.pst.TableEvent.Cell;

/**
 * Logical event fired when a cell is highlighted.
 */
public class CellHighlightEvent extends HighlightEvent<Cell> {

  /**
   * Construct a new {@link CellHighlightEvent}.
   * 
   * @param rowIndex the index of the highlighted row
   * @param cellIndex the index of the highlighted cell
   */
  public CellHighlightEvent(int rowIndex, int cellIndex) {
    this(new Cell(rowIndex, cellIndex));
  }

  /**
   * Construct a new {@link CellHighlightEvent}.
   * 
   * @param cell the cell being highlighted
   */
  public CellHighlightEvent(Cell cell) {
    super(cell);
  }

}
