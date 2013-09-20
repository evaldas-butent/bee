package com.butent.bee.client.grid.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class SelectionHeader extends AbstractCell<String> {

  private static final InputElement INPUT;

  static {
    INPUT = Document.get().createCheckInputElement();
    INPUT.addClassName("bee-SelectionHeader");
  }

  private boolean checked;

  public SelectionHeader() {
    super(EventUtils.EVENT_TYPE_CLICK);
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
      ValueUpdater<String> valueUpdater) {

    if (EventUtils.isClick(event) && context instanceof CellContext) {
      setChecked(!isChecked());
      CellGrid grid = ((CellContext) context).getGrid();

      if (grid != null && !BeeUtils.isEmpty(grid.getRowData())) {
        for (int i = 0; i < grid.getRowData().size(); i++) {
          IsRow row = grid.getRowData().get(i);
          if (grid.isRowSelected(row.getId()) != isChecked()) {
            grid.selectRow(i, row);
          }
        }
      }

    } else {
      super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }
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
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (context instanceof CellContext) {
      updateValue(((CellContext) context).getGrid());
    }

    INPUT.setChecked(isChecked());
    INPUT.setDefaultChecked(isChecked());

    sb.append(SafeHtmlUtils.fromTrustedString(INPUT.getString()));
  }

  private boolean isChecked() {
    return checked;
  }

  private void setChecked(boolean checked) {
    this.checked = checked;
  }

  private void updateValue(CellGrid grid) {
    if (grid != null && !BeeUtils.isEmpty(grid.getRowData())) {
      int countSelected = 0;

      for (IsRow row : grid.getRowData()) {
        if (grid.isRowSelected(row.getId())) {
          countSelected++;
        }
      }

      setChecked(countSelected > grid.getRowData().size() - countSelected);
    }
  }
}
