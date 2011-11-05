package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.event.HandlesDeleteEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collections;
import java.util.List;

/**
 * Implements usage of data views in user interface.
 */

public class DataInfoProvider implements HandlesDeleteEvents, RowInsertEvent.Handler {

  public abstract static class DataInfoCallback implements Callback<DataInfo, String[]> {
    public void onFailure(String[] reason) {
      BeeKeeper.getScreen().notifySevere(reason);
    }

    public abstract void onSuccess(DataInfo result);
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

  public DataInfoProvider() {
    super();
  }

  public void create() {
    getDataInfoCreator().execute();
  }
  
  public void getDataInfo(final String viewName, final DataInfoCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(callback);
    
    DataInfo dataInfo = getDataInfo(viewName);
    if (dataInfo != null) {
      callback.onSuccess(dataInfo);
      return;
    }

    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_DATA_INFO);
    params.addQueryItem(Service.VAR_VIEW_NAME, viewName);

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);
        
        if (response.hasResponse(DataInfo.class)) {
          DataInfo result = DataInfo.restore((String) response.getResponse());
          getViews().add(result);
          callback.onSuccess(result);
        } else if (response.hasErrors()) {
          callback.onFailure(response.getErrors());
        } else {
          callback.onFailure(new String[]{"get data info", viewName, "invalid response"});
        }
      }
    });
  }

  public DataInfoCreator getDataInfoCreator() {
    return dataInfoCreator;
  }

  public List<DataInfo> getViews() {
    return views;
  }

  public void loadDataInfo() {
    BeeKeeper.getRpc().makeGetRequest(Service.GET_DATA_INFO, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);
        String[] info = Codec.beeDeserializeCollection((String) response.getResponse());

        getViews().clear();
        for (String s : info) {
          getViews().add(DataInfo.restore(s));
        }
        Collections.sort(getViews());

        if (getDataInfoWidget() == null) {
          showDataInfo();
        } else {
          getDataInfoWidget().setRowData(getViews());
        }
      }
    });
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

    Column<DataInfo, Number> columnCountColumn = new Column<DataInfo, Number>(new NumberCell()) {
      @Override
      public Number getValue(DataInfo object) {
        return (object == null) ? BeeConst.UNDEF : object.getColumnCount();
      }
    };
    columnCountColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    grid.addColumn(columnCountColumn);

    Column<DataInfo, Number> rowCountColumn = new Column<DataInfo, Number>(new NumberCell()) {
      @Override
      public Number getValue(DataInfo object) {
        return (object == null) ? BeeConst.UNDEF : object.getRowCount();
      }
    };
    rowCountColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    grid.addColumn(rowCountColumn);

    grid.setRowData(getViews());

    final SingleSelectionModel<DataInfo> selector = new SingleSelectionModel<DataInfo>();
    selector.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        DataInfo info = selector.getSelectedObject();
        if (info != null && selector.isSelected(info) && info.getRowCount() >= 0) {
          GridFactory.openGrid(info.getName());
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

  private DataInfo getDataInfo(String name) {
    for (DataInfo dataInfo : getViews()) {
      if (BeeUtils.same(dataInfo.getName(), name)) {
        return dataInfo;
      }
    }
    return null;
  }

  private CellTable<DataInfo> getDataInfoWidget() {
    return dataInfoWidget;
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
}
