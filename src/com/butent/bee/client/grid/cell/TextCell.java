package com.butent.bee.client.grid.cell;

import com.butent.bee.client.grid.CellContext;

public class TextCell extends AbstractCell<String> {

  public TextCell() {
    super();
  }

  @Override
  public String render(CellContext context, String value) {
    return value;
  }
}
