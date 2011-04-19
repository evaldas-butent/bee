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
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class Explorer {

  private class DataInfoCallback implements ResponseCallback {
    private boolean show;

    private DataInfoCallback(boolean show) {
      this.show = show;
    }

    public void onResponse(JsArrayString arr) {
      Assert.notNull(arr);
      Assert.isTrue(arr.length() >= 1);
      String[] info = Codec.beeDeserialize(arr.get(0));

      getViews().clear();
      for (String s : info) {
        getViews().add(DataInfo.restore(s));
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

      if (getViews().isEmpty()) {
        BeeKeeper.getUi().updateData(new BeeImage(Global.getImages().loading()), false);
        loadDataInfo(true);
      } else {
        showDataInfo();
      }
    }
  }

  private final List<DataInfo> views = Lists.newArrayList();
  private final DataInfoCreator dataInfoCreator = new DataInfoCreator();

  private int asyncThreshold = 100;

  public Explorer() {
    super();
  }

  public Explorer(int asyncThreshold) {
    this();
    setAsyncThreshold(asyncThreshold);
  }

  public int getAsyncThreshold() {
    return asyncThreshold;
  }

  public DataInfoCreator getDataInfoCreator() {
    return dataInfoCreator;
  }

  public List<DataInfo> getViews() {
    return views;
  }

  public void loadDataInfo(boolean show) {
    BeeKeeper.getRpc().makeGetRequest(Service.GET_VIEW_LIST, new DataInfoCallback(show));
  }

  public void setAsyncThreshold(int asyncThreshold) {
    this.asyncThreshold = asyncThreshold;
  }

  public void showDataInfo() {
    if (getViews().isEmpty()) {
      BeeKeeper.getUi().updateData(new BeeLabel("Data not available"), false);
      return;
    }

    CellTable<DataInfo> grid = new CellTable<DataInfo>(getViews().size());

    Column<DataInfo, String> nameColumn = new TextColumn<DataInfo>() {
      @Override
      public String getValue(DataInfo object) {
        return (object == null) ? BeeConst.STRING_EMPTY : object.getName();
      }
    };
    grid.addColumn(nameColumn);

    Column<DataInfo, Number> countColumn = new Column<DataInfo, Number>(new NumberCell()) {
      @Override
      public Number getValue(DataInfo object) {
        return (object == null) ? -1 : object.getRowCount();
      }
    };
    countColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    grid.addColumn(countColumn);

    grid.setRowData(getViews());

    final SingleSelectionModel<DataInfo> selector = new SingleSelectionModel<DataInfo>();
    selector.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        DataInfo info = selector.getSelectedObject();
        if (info != null && selector.isSelected(info)) {
          openView(info);
          selector.setSelected(info, false);
        }
      }
    });
    grid.setSelectionModel(selector);

    BeeKeeper.getUi().updateData(grid, true);
  }

  private void openView(final DataInfo dataInfo) {
    int rc = dataInfo.getRowCount();
    if (rc < 0) {
      BeeKeeper.getLog().info(dataInfo.getName(), "not active");
      return;
    }

    int limit = getAsyncThreshold();
    final boolean async;
    if (limit > 0 && rc > limit) {
      async = true;
    } else {
      async = false;
      limit = -1;
    }

    Queries.getRowSet(dataInfo.getName(), null, null, 0, limit, new Queries.RowSetCallback() {
      public void onResponse(BeeRowSet rowSet) {
        if (rowSet.isEmpty()) {
          BeeKeeper.getLog().info(rowSet.getViewName(), "RowSet is empty");
        } else {
          showView(dataInfo, rowSet, async);
        }
      }
    });
  }

  private void showView(DataInfo dataInfo, BeeRowSet rowSet, boolean async) {
    GridPresenter presenter = new GridPresenter(dataInfo, rowSet, async);
    presenter.start(BeeKeeper.getUi().getActivePanelWidth(),
        BeeKeeper.getUi().getActivePanelHeight());
    BeeKeeper.getUi().updateActivePanel(presenter.getWidget());
  }
}
