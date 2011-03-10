package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.TableInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class Cache {
  private class DataInfoCallback implements ResponseCallback {
    private boolean show;

    private DataInfoCallback(boolean show) {
      this.show = show;
    }

    public void onResponse(JsArrayString arr) {
      Assert.notNull(arr);
      Assert.isTrue(arr.length() >= 1);
      String[] info = Codec.beeDeserialize(Codec.beeDeserialize(arr.get(0))[0]);

      getTables().clear();
      for (String s : info) {
        getTables().add(TableInfo.restore(s));
      }
      if (show) {
        showDataInfo();
      }
    }
  }

  private class DataInfoCreator extends BeeCommand {
    @Override
    public void execute() {
      BeeLayoutPanel panel = BeeKeeper.getUi().getDataPanel();
      if (panel.getWidgetCount() > 0) {
        return;
      }
      if (!BeeKeeper.getUser().isLoggedIn()) {
        return;
      }

      if (getTables().isEmpty()) {
        BeeKeeper.getUi().updateData(new BeeImage(Global.getImages().loading()), false);
        loadDataInfo(true);
      } else {
        showDataInfo();
      }
    }
  }

  private class PrimaryKeyCallback implements ResponseCallback {
    private String table;
    private ResponseCallback callback;

    private PrimaryKeyCallback(String table, ResponseCallback callback) {
      this.table = table;
      this.callback = callback;
    }

    @Override
    public void onResponse(JsArrayString arr) {
      Assert.notNull(arr);
      Assert.isTrue(arr.length() >= 1);
      String column = arr.get(0);
      Assert.notEmpty(column);

      setPrimaryKey(table, column);
      BeeKeeper.getLog().info(table, column);

      callback.onResponse(arr);
    }
  }

  private static String primaryKeyPrefix = "pk-";

  private List<TableInfo> tables = Lists.newArrayList();
  private DataInfoCreator dataInfoCreator = new DataInfoCreator();

  public Cache() {
  }

  public DataInfoCreator getDataInfoCreator() {
    return dataInfoCreator;
  }

  public void getPrimaryKey(String table, ResponseCallback callback) {
    Assert.notEmpty(table);
    Assert.notNull(callback);

    String value = BeeKeeper.getStorage().getItem(pkName(table));
    if (!BeeUtils.isEmpty(value)) {
      callback.onResponse(JsUtils.createArray(value));
      return;
    }

    ParameterList params = BeeKeeper.getRpc().createParameters(BeeService.SERVICE_DB_PRIMARY);
    params.addPositionalHeader(table);
    BeeKeeper.getRpc().makeGetRequest(params, new PrimaryKeyCallback(table, callback));
  }

  public List<TableInfo> getTables() {
    return tables;
  }

  public void loadDataInfo(boolean show) {
    BeeKeeper.getRpc().makeGetRequest("rpc_ui_data_info", new DataInfoCallback(show));
  }

  public void setPrimaryKey(String ref) {
    Assert.notEmpty(ref);
    String table = BeeUtils.getPrefix(ref, BeeConst.CHAR_POINT);
    String column = BeeUtils.getSuffix(ref, BeeConst.CHAR_POINT);

    setPrimaryKey(table, column);
  }

  public void setPrimaryKey(String table, String column) {
    Assert.notEmpty(table);
    Assert.notEmpty(column);

    BeeKeeper.getStorage().setItem(pkName(table), column.trim());
  }

  public void showDataInfo() {
    if (getTables().isEmpty()) {
      BeeKeeper.getUi().updateData(new BeeLabel("Data not available"), false);
      return;
    }

    CellTable<TableInfo> grid = new CellTable<TableInfo>(getTables().size());

    Column<TableInfo, String> nameColumn = new TextColumn<TableInfo>() {
      @Override
      public String getValue(TableInfo object) {
        return (object == null) ? BeeConst.STRING_EMPTY : object.getName();
      }
    };
    grid.addColumn(nameColumn);

    Column<TableInfo, Number> countColumn = new Column<TableInfo, Number>(new NumberCell()) {
      @Override
      public Number getValue(TableInfo object) {
        return (object == null) ? -1 : object.getRowCount();
      }
    };
    countColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LOCALE_END);
    grid.addColumn(countColumn);

    grid.setRowData(getTables());

    final SingleSelectionModel<TableInfo> selector = new SingleSelectionModel<TableInfo>();
    selector.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        TableInfo info = selector.getSelectedObject();
        if (info != null) {
          openTable(info);
        }
      }
    });
    grid.setSelectionModel(selector);

    BeeKeeper.getUi().updateData(grid, true);
  }

  private void openTable(TableInfo ti) {
    if (ti.getRowCount() < 0) {
      BeeKeeper.getLog().info(ti.getName(), "not active");
      return;
    }
    BeeKeeper.getRpc().makePostRequest("rpc_ui_table",
        XmlUtils.createString(BeeService.XML_TAG_DATA, "table_name", ti.getName()),
        new ResponseCallback() {
          @Override
          public void onResponse(JsArrayString arr) {
            Assert.notNull(arr);
            BeeRowSet rs = BeeRowSet.restore(arr.get(0));
            if (rs.isEmpty()) {
              BeeKeeper.getLog().info(rs.getViewName(), "RowSet is empty");
            } else {
              showTable(rs);
            }
          }
        }
        );
  }

  private String pkName(String table) {
    return primaryKeyPrefix + table.trim().toLowerCase();
  }

  private void showTable(BeeRowSet rs) {
    Split sp = new Split();
    sp.addNorth(new BeeLabel(rs.getViewName()), 20);
    sp.addSouth(new BeeLabel(BeeUtils.concat(1, rs.getNumberOfRows(), rs.getNumberOfColumns())), 20);
    sp.add(Global.simpleGrid(rs), true);
    BeeKeeper.getUi().updateActivePanel(sp);
  }
}
