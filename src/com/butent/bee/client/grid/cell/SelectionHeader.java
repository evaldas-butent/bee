package com.butent.bee.client.grid.cell;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.EventState;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.CellType;
import com.butent.bee.shared.utils.BeeUtils;

public class SelectionHeader extends AbstractCell<String> {

  private static final InputElement INPUT;

  static {
    INPUT = Document.get().createCheckInputElement();
    INPUT.addClassName(BeeConst.CSS_CLASS_PREFIX + "SelectionHeader");
  }

  private boolean checked;

  public SelectionHeader() {
    super(EventUtils.EVENT_TYPE_CLICK);
  }

  @Override
  public CellType getCellType() {
    return CellType.HTML;
  }

  @Override
  public EventState onBrowserEvent(CellContext context, Element parent, String value, Event event) {
    EventState state = super.onBrowserEvent(context, parent, value, event);

    if (state.proceed() && EventUtils.isClick(event)) {
      setChecked(!isChecked());
      CellGrid grid = context.getGrid();

      if (grid != null && !BeeUtils.isEmpty(grid.getRowData())) {
        for (int i = 0; i < grid.getRowData().size(); i++) {
          IsRow row = grid.getRowData().get(i);
          if (grid.isRowSelected(row.getId()) != isChecked()) {
            grid.toggleRowSelection(i, row, false);
          }
        }
      }

      state = EventState.CONSUMED;
    }

    return state;
  }

  public void refresh(CellGrid grid, Element cellElement) {
    if (grid != null && cellElement != null) {
      boolean wasChecked = isChecked();
      updateValue(grid);

      if (isChecked() != wasChecked) {
        DomUtils.setCheckValue(cellElement, isChecked());
      }
    }
  }

  @Override
  public String render(CellContext context, String value) {
    updateValue(context.getGrid());

    INPUT.setChecked(isChecked());
    INPUT.setDefaultChecked(isChecked());

    return INPUT.getString();
  }

  private boolean isChecked() {
    return checked;
  }

  private void setChecked(boolean checked) {
    this.checked = checked;
  }

  private void updateValue(CellGrid grid) {
    if (grid != null && !BeeUtils.isEmpty(grid.getRowData())) {
      boolean value = true;

      for (IsRow row : grid.getRowData()) {
        if (!grid.isRowSelected(row.getId())) {
          value = false;
          break;
        }
      }

      setChecked(value);
    }
  }
}
