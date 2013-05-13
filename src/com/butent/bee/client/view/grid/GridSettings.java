package com.butent.bee.client.view.grid;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.view.grid.CellGrid.ColumnInfo;
import com.butent.bee.client.widget.BooleanWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.SimpleBoolean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class GridSettings {

  private static final String STYLE_PREFIX = "bee-GridSettings-";
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";
  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_CHECK = STYLE_PREFIX + "check";
  private static final String STYLE_LABEL = STYLE_PREFIX + "label";
  
  public static void handle(final CellGrid grid, UIObject target) {
    Assert.notNull(grid);
    if (grid.getRowData().isEmpty()) {
      return;
    }

    List<ColumnInfo> predefinedColumns = grid.getPredefinedColumns();
    List<Integer> visibleColumns = grid.getVisibleColumns();

    final HtmlTable table = new HtmlTable();
    table.addStyleName(STYLE_TABLE);

    int row = 0;

    for (int index : visibleColumns) {
      table.setWidget(row, 0, createCheckBox(true));
      table.setWidget(row, 1, createLabel(predefinedColumns.get(index), index));

      row++;
    }

    if (predefinedColumns.size() > visibleColumns.size()) {
      for (int i = 0; i < predefinedColumns.size(); i++) {
        if (!visibleColumns.contains(i)) {
          table.setWidget(row, 0, createCheckBox(false));
          table.setWidget(row, 1, createLabel(predefinedColumns.get(i), i));

          row++;
        }
      }
    }
    
    Global.inputWidget("Stulpeliai", table, new InputCallback() {
      @Override
      public void onSuccess() {
        List<Integer> selectedColumns = Lists.newArrayList();

        for (int i = 0; i < table.getRowCount(); i++) {
          Widget checkBox = table.getWidget(i, 0);
          if (checkBox instanceof BooleanWidget 
              && BeeUtils.isTrue(((BooleanWidget) checkBox).getValue())) {
            int index = DomUtils.getDataIndex(table.getWidget(i, 1).getElement());
            if (!BeeConst.isUndef(index)) {
              selectedColumns.add(index);
            }
          }
        }
        
        commitColumns(grid, selectedColumns);
      }
    }, STYLE_DIALOG, target);
  }
  
  private static void commitColumns(CellGrid grid, List<Integer> columns) {
    if (!columns.isEmpty() && !columns.equals(grid.getVisibleColumns())) {
      BeeUtils.overwrite(grid.getVisibleColumns(), columns);
      
      grid.getRenderedRows().clear();

      grid.getResizedRows().clear();
      grid.getResizedCells().clear();
      
      grid.onResize();
    }
  }
  
  private static Widget createCheckBox(boolean value) {
    SimpleBoolean widget = new SimpleBoolean(value);
    widget.addStyleName(STYLE_CHECK);
    return widget;
  }

  private static Widget createLabel(ColumnInfo columnInfo, int index) {
    CustomDiv widget = new CustomDiv(STYLE_LABEL);

    widget.setHTML(getLabel(columnInfo));
    DomUtils.setDataIndex(widget.getElement(), index);
    
    return widget;
  }
  
  private static String getLabel(ColumnInfo columnInfo) {
    return BeeUtils.notEmpty(columnInfo.getCaption(), columnInfo.getColumnId());
  }
}
