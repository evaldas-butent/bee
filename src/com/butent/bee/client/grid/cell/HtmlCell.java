package com.butent.bee.client.grid.cell;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.shared.ui.CellType;

/**
 * Manages rendering of cells containing arbitrary html.
 */

public class HtmlCell extends AbstractCell<String> {

  public HtmlCell() {
    super();
  }

  @Override
  public CellType getCellType() {
    return CellType.HTML;
  }

  @Override
  public String render(CellContext context, String value) {
    return value;
  }
}
