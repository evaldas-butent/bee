package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ImportPropertiesGrid extends AbstractGridInterceptor {

  private final ImportOptionForm form;

  public ImportPropertiesGrid(ImportOptionForm form) {
    Assert.notNull(form);
    this.form = form;
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    addNewProperty();
    return false;
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {

    if (BeeUtils.same(columnName, COL_IMPORT_PROPERTY)) {
      return new AbstractCellRenderer(null) {
        @Override
        public String render(IsRow row) {
          String[] props = form.getProperties();
          Integer idx = Data.getInteger(TBL_IMPORT_PROPERTIES, row, COL_IMPORT_PROPERTY);

          if (ArrayUtils.isIndex(props, idx)) {
            return props[idx];
          }
          return BeeUtils.toString(idx);
        }
      };
    }
    return super.getRenderer(columnName, dataColumns, columnDescription);
  }

  private void addNewProperty() {
    String[] props = form.getProperties();
    Map<Integer, String> map = Maps.newLinkedHashMap();

    if (!ArrayUtils.isEmpty(props)) {
      for (int i = 0; i < props.length; i++) {
        map.put(i, props[i]);
      }
      int propIdx = getGridView().getDataIndex(COL_IMPORT_PROPERTY);

      for (IsRow row : getGridView().getRowData()) {
        map.remove(row.getInteger(propIdx));
      }
    }
    if (BeeUtils.isEmpty(map)) {
      getGridView().notifyWarning(Localized.getConstants().noData());
      return;
    }
    HtmlTable table = new HtmlTable();
    table.setColumnCellClasses(0, StyleUtils.NAME_REQUIRED);
    int row = 0;

    final ListBox property = new ListBox();
    table.setHtml(row, 0, Localized.getConstants().trImportProperty());
    table.setWidget(row, 1, property);
    row++;

    for (Entry<Integer, String> idx : map.entrySet()) {
      property.addItem(idx.getValue(), BeeUtils.toString(idx.getKey()));
    }
    final InputText value = new InputText();
    table.setHtml(row, 0, Localized.getConstants().trImportValue());
    table.setWidget(row, 1, value);
    row++;

    Global.inputWidget(Localized.getConstants().trImportNewProperty(), table, new InputCallback() {
      @Override
      public String getErrorMessage() {
        if (BeeUtils.isEmpty(property.getValue())) {
          property.setFocus(true);
        } else if (BeeUtils.isEmpty(value.getValue())) {
          value.setFocus(true);
        } else {
          return super.getErrorMessage();
        }
        return Localized.getConstants().valueRequired();
      }

      @Override
      public void onSuccess() {
        getGridView().ensureRelId(new IdCallback() {
          @Override
          public void onSuccess(Long id) {
            Queries.insert(TBL_IMPORT_PROPERTIES, getGridView().getDataColumns(),
                Lists.newArrayList(BeeUtils.toString(id), property.getValue(), value.getValue()),
                null, new RowInsertCallback(TBL_IMPORT_PROPERTIES) {
                  @Override
                  public void onSuccess(BeeRow newRow) {
                    super.onSuccess(newRow);
                    getGridView().getGrid().insertRow(newRow, true);
                  }
                });
          }
        });
      }
    }, null, getGridView().getElement(), null);
  }
}
