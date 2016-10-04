package com.butent.bee.client.grid.cell;

import com.butent.bee.client.grid.CellContext;

public class CalculatedCell extends AbstractCell<String> {

  public CalculatedCell() {
    super();
  }

  @Override
  public String render(CellContext context, String value) {
    return value;
  }
}
