package com.butent.bee.client.grid.cell;

import com.butent.bee.client.grid.CellContext;

public class FooterCell extends AbstractCell<String> {

  public FooterCell() {
    super();
  }

  @Override
  public String render(CellContext context, String value) {
    return value;
  }
}
