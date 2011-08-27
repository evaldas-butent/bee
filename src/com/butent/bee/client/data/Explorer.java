package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.HandlesDeleteEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

/**
 * Implements usage of data views in user interface.
 */

public class Explorer implements HandlesDeleteEvents, RowInsertEvent.Handler {

  private class DataInfoCallback implements ResponseCallback {
    private DataInfoCallback() {
    }

    public void onResponse(ResponseObject response) {
      Assert.notNull(response);
      String[] info = Codec.beeDeserializeCollection((String) response.getResponse());

      getViews().clear();
      for (String s : info) {
        getViews().add(DataInfo.restore(s));
      }

      if (getDataInfoWidget() == null) {
        showDataInfo();
      } else {
        getDataInfoWidget().setRowData(getViews());
      }
    }
  }

  private class DataInfoCreator extends BeeCommand {
    @Override
    public void execute() {
      if (getDataInfoWidget() == null) {
        BeeKeeper.getScreen().updateData(ensureLoadingWidget());
      }
      loadDataInfo();
    }
  }

  private final List<DataInfo> views = Lists.newArrayList();
  private final DataInfoCreator dataInfoCreator = new DataInfoCreator();

  private CellTable<DataInfo> dataInfoWidget = null;
  private Widget loadingWidget = null;

  public Explorer() {
    super();
  }

  public void create() {
    getDataInfoCreator().execute();
  }

  public DataInfo getDataInfo(String name) {
    Assert.notNull(name);
    for (DataInfo dataInfo : getViews()) {
      if (BeeUtils.same(dataInfo.getName(), name)) {
        return dataInfo;
      }
    }
    return null;
  }

  public DataInfoCreator getDataInfoCreator() {
    return dataInfoCreator;
  }

  public List<DataInfo> getViews() {
    return views;
  }

  public void loadDataInfo() {
    BeeKeeper.getRpc().makeGetRequest(Service.GET_VIEW_LIST, new DataInfoCallback());
  }

  public void onMultiDelete(MultiDeleteEvent event) {
    DataInfo dataInfo = getDataInfo(event.getViewName());
    if (dataInfo != null) {
      dataInfo.setRowCount(dataInfo.getRowCount() - event.getRows().size());
      refresh();
    }
  }

  public void onRowDelete(RowDeleteEvent event) {
    DataInfo dataInfo = getDataInfo(event.getViewName());
    if (dataInfo != null) {
      dataInfo.setRowCount(dataInfo.getRowCount() - 1);
      refresh();
    }
  }

  public void onRowInsert(RowInsertEvent event) {
    DataInfo dataInfo = getDataInfo(event.getViewName());
    if (dataInfo != null) {
      dataInfo.setRowCount(dataInfo.getRowCount() + 1);
      refresh();
    }
  }

  public void openView(final DataInfo dataInfo) {
    if (dataInfo.getRowCount() < 0) {
      BeeKeeper.getLog().info(dataInfo.getName(), "not active");
      return;
    }

    BeeKeeper.getScreen().updateActivePanel(ensureLoadingWidget());

    GridFactory.getGrid(dataInfo.getName(), new GridFactory.GridCallback() {
      public void onFailure(String[] reason) {
        BeeKeeper.getScreen().notifyWarning(reason);
        getInitialRowSet(dataInfo, null);
      }

      public void onSuccess(GridDescription result) {
        getInitialRowSet(dataInfo, result);
      }
    });
  }

  public void refresh() {
    if (getDataInfoWidget() != null) {
      getDataInfoWidget().redraw();
    }
  }

  public void showDataInfo() {
    if (getViews().isEmpty()) {
      BeeKeeper.getScreen().updateData(new BeeLabel("Data not available"));
      return;
    }

    CellTable<DataInfo> grid = new CellTable<DataInfo>(getViews().size());
    setDataInfoWidget(grid);

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

    BeeKeeper.getScreen().updateData(grid);
  }

  private Widget ensureLoadingWidget() {
    if (getLoadingWidget() == null) {
      setLoadingWidget(new BeeImage(Global.getImages().loading()));
    }
    return getLoadingWidget();
  }

  private CellTable<DataInfo> getDataInfoWidget() {
    return dataInfoWidget;
  }

  private void getInitialRowSet(final DataInfo dataInfo, final GridDescription gridDescription) {
    int limit = (gridDescription == null) ? DataUtils.getDefaultAsyncThreshold()
        : BeeUtils.unbox(gridDescription.getAsyncThreshold());
    int rc = dataInfo.getRowCount();

    final boolean async;
    if (rc >= limit) {
      async = true;
      if (rc <= DataUtils.getMaxInitialRowSetSize()) {
        limit = -1;
      } else {
        limit = DataUtils.getMaxInitialRowSetSize();
      }
    } else {
      async = false;
      limit = -1;
    }

    Queries.getRowSet(dataInfo.getName(), null, null, null, 0, limit, CachingPolicy.FULL,
        new Queries.RowSetCallback() {
          public void onFailure(String[] reason) {
          }

          public void onSuccess(final BeeRowSet rowSet) {
            showView(dataInfo, rowSet, async, gridDescription);
          }
        });
  }

  private Widget getLoadingWidget() {
    return loadingWidget;
  }

  private void setDataInfoWidget(CellTable<DataInfo> dataInfoWidget) {
    this.dataInfoWidget = dataInfoWidget;
  }

  private void setLoadingWidget(Widget loadingWidget) {
    this.loadingWidget = loadingWidget;
  }

  private void showView(DataInfo dataInfo, BeeRowSet rowSet, boolean async,
      GridDescription gridDescription) {
    GridPresenter presenter = new GridPresenter(dataInfo, rowSet, async, gridDescription, false);
    BeeKeeper.getScreen().updateActivePanel(presenter.getWidget());
  }
}
